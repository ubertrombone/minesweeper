package com.minesweeperMobile.minesweeper

import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.minesweeperMobile.Difficulties.*
import com.minesweeperMobile.Markers.*
import com.minesweeperMobile.Numbers.*
import com.minesweeperMobile.R
import com.minesweeperMobile.databinding.FragmentMinesweeperBinding
import com.minesweeperMobile.finalmessage.FinalMessageFragment
import com.minesweeperMobile.leaderboard.LeaderBoardFragment
import com.minesweeperMobile.model.MinesweeperViewModel
import com.minesweeperMobile.newgame.NewGameFragment
import com.minesweeperMobile.results.ResultsFragment
import com.minesweeperMobile.settings.SettingsFragment
import com.minesweeperMobile.username.UsernameFragment
import java.lang.IllegalStateException
import kotlin.math.roundToInt

class MinesweeperFragment: Fragment() {

    private var binding: FragmentMinesweeperBinding? = null
    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var shovelEmptySwitch = false
    private var mineSelectedOnEmptySwitch = false
    private var checkIfWinMessageHasAlreadyAppeared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(getDrawable(requireContext(), R.drawable.dialog_straight))
        (activity as AppCompatActivity).supportActionBar?.elevation = 0F
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").getReference("${auth.uid}/")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentMinesweeperBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            minesweeperFragment = this@MinesweeperFragment
        }

        observeUserState()
        binding?.fabButtons?.layoutDirection = if (sharedViewModel.fabButtonRTL.value) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        binding?.fabMine?.isEnabled = sharedViewModel.mineAssistFAB.value
        if (sharedViewModel.mineAssistFAB.value) binding?.fabMine?.visibility = View.VISIBLE
        try { if (sharedViewModel.username.dataValue.value == false) createUsernameDialog() } catch (e: IllegalStateException) {}
        startGame(view)
        observeMineCounter()
        restartGameButtonClickListener()
    }

    private fun observeUserState() {
        sharedViewModel.username.dataValue.observe(viewLifecycleOwner) { if (!it) createUsernameDialog() }
    }

    private fun createUsernameDialog() {
        UsernameFragment.newInstance(getString(R.string.username_title))
            .show(childFragmentManager, UsernameFragment.TAG)
    }

    private fun startGame(view: View) {
        checkIfWinMessageHasAlreadyAppeared = false
        binding?.mineCounter?.text = sharedViewModel.howManyMines.value.toString()
        val mainLinearLayout = binding?.mineLay as LinearLayout
        (0 until sharedViewModel.height.value).forEach { i ->
            val rowLinearLayout = LinearLayout(requireContext())
            rowLinearLayout.orientation = LinearLayout.HORIZONTAL
            (0 until sharedViewModel.width.value).forEach { j ->
                val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val displayMetrics = DisplayMetrics()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) activity?.display?.getRealMetrics(displayMetrics)
                else @Suppress("DEPRECATION") activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

                val displayWidth = ((displayMetrics.widthPixels - (displayMetrics.widthPixels *.1)) / sharedViewModel.width.value).roundToInt()

                if (displayWidth < 62) {
                    params.height = getParams(sharedViewModel.width.value)
                    params.width = getParams(sharedViewModel.width.value)
                } else {
                    params.height = displayWidth
                    params.width = displayWidth
                }

                val cardView = setupUICardView(i, j, params)
                val cardLinearLayout = LinearLayout(requireContext())
                cardLinearLayout.orientation = LinearLayout.VERTICAL

                cardView.setOnClickListener {
                    sharedViewModelSetters(cardView)
                    getCoordsAndSetupClickListeners(view, cardView, true)
                }

                cardView.setOnLongClickListener {
                    sharedViewModelSetters(cardView)
                    getCoordsAndSetupClickListeners(view, cardView,false)
                    true
                }

                cardView.addView(cardLinearLayout)
                rowLinearLayout.addView(cardView)
            }
            mainLinearLayout.addView(rowLinearLayout)
        }
    }

    private fun setupUICardView(i: Int, j: Int, params: ViewGroup.LayoutParams): CardView {
        val cardView = CardView(requireContext())
        cardView.setContentPadding(0, 0, 0, 0)
        cardView.id = if (i == 0) j else sharedViewModel.width.value * i + j
        cardView.setBackgroundColor(getColor(requireContext(), sharedViewModel.getCardBackgroundColor(cardView.id,
            R.color.cyan_900,
            R.color.cyan_dark
        )))
        cardView.layoutParams = params
        return cardView
    }

    private fun onClickSettings(flagAlpha: Float, flagEnable: Boolean, axeAlpha: Float, axeEnable: Boolean, shovelSwitch: Boolean) {
        fabFlagSettings(flagAlpha, flagEnable)
        fabAxeSettings(axeAlpha, axeEnable)
        shovelEmptySwitch = shovelSwitch
    }

    private fun sharedViewModelSetters(cardView: CardView) {
        try { resetBackgroundColorAfterSelection() } catch (e: ClassCastException) {}

        sharedViewModel.selectedCardId.changeValue(cardView.id)
        // ID is zeroed and needs to be offset by 1 when converting to coords
        sharedViewModel.getCurrentCoords(cardView.id + 1)
        sharedViewModel.selectedCellBackgroundId.changeValue(cardView.id + 2500)
    }

    private fun getCoordsAndSetupClickListeners(view: View, cardView: CardView, shortClick: Boolean) {
        val coords = sharedViewModel.currentCoords
        val coordsValue = try {
            sharedViewModel.minefieldWithNumbers[coords[1]][coords[0]]
        } catch (e: ArrayIndexOutOfBoundsException) { "0" }

        if (shortClick) onShortClickListener(view, coords, coordsValue, cardView)
        else onLongClickListener(coords, coordsValue, cardView)
    }

    private fun onShortClickListener(view: View, coords: List<Int>, coordsValue: String, cardView: CardView) {
        val findViewOfCurrentSelected = view.findViewById<CardView>(sharedViewModel.selectedCardId.value)
        findViewOfCurrentSelected.setBackgroundColor(getColor(requireContext(), R.color.red_200))
        val possibleImageId = requireActivity().findViewById<ImageView>(sharedViewModel.getFlagId())

        when {
            sharedViewModel.firstMoveSwitch.value == 0 -> clickTheShovel()
            sharedViewModel.listOfSelections.contains(coords) && !sharedViewModel.listOfFlags.contains(coords) && sharedViewModel.listOfNumbers.contains(coordsValue) -> {
                onClickSettings(.25F, false, 1F, true, shovelSwitch = true)
            }
            cardView.children.contains(possibleImageId) -> onClickSettings(1F, true, .25F, false, shovelSwitch = false)
            else -> onClickSettings(1F, true, 1F, true, shovelSwitch = false)
        }
    }

    private fun onLongClickListener(coords: List<Int>, coordsValue: String, cardView: CardView) {
        when {
            sharedViewModel.firstMoveSwitch.value == 0 -> clickTheShovel()
            sharedViewModel.listOfSelections.contains(coords) && !sharedViewModel.listOfFlags.contains(coords) && sharedViewModel.listOfNumbers.contains(coordsValue) -> {
                buttonSelect()
                onEmptySelected(sharedViewModel.currentCoords[0], sharedViewModel.currentCoords[1], true)
                sharedViewModel.moveCounter.increment()
            }
            else -> {
                buttonSelect()
                val possibleImageId = requireActivity().findViewById<ImageView>(sharedViewModel.getFlagId())
                if (cardView.children.contains(possibleImageId)) removeFlag(cardView, possibleImageId, coords, coords[0], coords[1])
                else placeFlag(cardView, coords, coords[0], coords[1])
            }
        }
    }

    private fun getParams(size: Int) = when {
        size <= 10 -> 100
        size <= 13 -> 76
        size <= 14 -> 70
        else -> 62
    }

    private fun buttonSelect() {
        fabFlagSettings(.25F, false)
        fabAxeSettings(.25F, false)
        try {
            requireActivity().findViewById<ImageView>(sharedViewModel.selectedCellBackgroundId.value).visibility = View.GONE
        } catch (e: ClassCastException) {}
        catch (e: NullPointerException) {}
    }

    private fun fabFlagSettings(alpha: Float, enable: Boolean) {
        binding?.fabFlag?.alpha = alpha
        binding?.fabFlag?.isEnabled = enable
    }

    private fun fabAxeSettings(alpha: Float, enable: Boolean) {
        binding?.fabAxe?.alpha = alpha
        binding?.fabAxe?.isEnabled = enable
    }

    fun clickTheShovel() {
        sharedViewModel.moveCounter.increment()

        try {
            val cardView = requireActivity().findViewById<CardView>(sharedViewModel.selectedCardId.value)
            val findViewOfPrevious = requireActivity().findViewById<ImageView>(2273546327529688.toInt())
            cardView.removeView(findViewOfPrevious)
        } catch (e: ClassCastException) {}

        if (sharedViewModel.firstMoveSwitch.value == 0) {
            binding?.timeCounter?.base = SystemClock.elapsedRealtime()
            binding?.timeCounter?.start()
        }
        sharedViewModel.move(sharedViewModel.selectedCardId.value)
        buttonSelect()

        if (shovelEmptySwitch) {
            onEmptySelected(sharedViewModel.currentCoords[0], sharedViewModel.currentCoords[1], true)
            shovelEmptySwitch = false
            return
        }

        sharedViewModel.addToSelections(sharedViewModel.currentCoords)
        when (sharedViewModel.getItemAtMinefieldPosition()) {
            MINE.mark -> onMineSelected()
            EMPTY.mark -> onEmptySelected(sharedViewModel.currentCoords[0], sharedViewModel.currentCoords[1], false)
            ONE.alphaNumber -> addImageView(ONE.number, true)
            TWO.alphaNumber -> addImageView(TWO.number, true)
            THREE.alphaNumber -> addImageView(THREE.number, true)
            FOUR.alphaNumber -> addImageView(FOUR.number, true)
            FIVE.alphaNumber -> addImageView(FIVE.number, true)
            SIX.alphaNumber -> addImageView(SIX.number, true)
            SEVEN.alphaNumber -> addImageView(SEVEN.number, true)
            EIGHT.alphaNumber -> addImageView(EIGHT.number, true)
        }
        createCardView(sharedViewModel.convertCoordsToNumber(sharedViewModel.currentCoords), background = false, clickable = false)
        checkAroundTheFlags()
    }

    private fun addImageView(image: Int, numberOrNot: Boolean) {
        // all of these image views will have their ID the same as the cardView + 7500
        createCardAndImage(image, sharedViewModel.selectedCardId.value, 7500, true)
        if (sharedViewModel.listOfSelections.distinct().size + sharedViewModel.mineCounter.value == sharedViewModel.height.value * sharedViewModel.width.value) {
            if (numberOrNot) gameOverMessage(R.string.win)
        }
    }

    fun clickMineAssist() {
        sharedViewModel.listOfFlags.forEach {
            val xVal = it[0]
            val yVal = it[1]

            (yVal - 1 .. yVal + 1).forEach { i -> (xVal - 1 .. xVal + 1).forEach j@{ j ->
                val coords = listOf(j, i)
                if (coords == listOf(xVal, yVal)) return@j
                if (!sharedViewModel.listOfSelections.contains(coords)) return@j
                if (sharedViewModel.listOfFlags.contains(coords)) return@j

                //Reduces the scope of Mine Assist by only checking cells with unselected cells around them.
                var numberIsSurroundedBySelectedCells = true
                for (k in (coords[1] - 1 .. coords[1] + 1)) { for (l in (coords[0] - 1 .. coords[0] + 1)) {
                    val newCoords = listOf(l, k)
                    if (!sharedViewModel.listOfSelections.contains(newCoords)) {
                        numberIsSurroundedBySelectedCells = false
                        break
                    }
                } }
                if (numberIsSurroundedBySelectedCells) return@j

                val cardView = requireActivity().findViewById<CardView>(sharedViewModel.convertCoordsToNumber(coords))
                if (cardView.isClickable) {
                    sharedViewModel.moveCounter.increment()
                    onEmptySelected(j, i, true)
                }
            } }
        }
    }

    // Mine Image: By KDE Kmines team: [1] - gnomine artwork., GPL, https://commons.wikimedia.org/w/index.php?curid=2102166
    private fun onMineSelected() {
        val mines = sharedViewModel.mineLocations

        if (!mineSelectedOnEmptySwitch) {
            addImageView(R.drawable.gnome_gnomine__2_, false)
            mines.remove(sharedViewModel.selectedCardId.value)
        }

        var minesLeft = sharedViewModel.mineLocations.size

        repeat(minesLeft) {
            val mine = mines.random()
            val imageOfFlag = requireActivity().findViewById<ImageView>(mine - 1 + 5000)
            val cardViewId = requireActivity().findViewById<CardView>(mine - 1)
            cardViewId.removeView(imageOfFlag)
            createCardAndImage(R.drawable.gnome_gnomine__2_, mine - 1, 0, false)
            mines.remove(mine)
            minesLeft --
        }

        gameOverMessage(R.string.loss)
    }

    private fun createCardAndImage(image: Int, cardId: Int, imageId: Int, imageIdOrNot: Boolean) {
        val imageView = createImageView(image)
        val cardView = createCardView(cardId, background = true, clickable = false)
        if (imageIdOrNot) imageView.id = cardView.id + imageId
        cardView.addView(imageView)
    }

    private fun onEmptySelected(xCoord: Int, yCoord: Int, number: Boolean) {
        sharedViewModel.clearEmptySelections()
        sharedViewModel.emptyCells(yCoord, xCoord, sharedViewModel.minefieldWithNumbers, number)

        sharedViewModel.emptySelections.distinct().forEach {
            val id = sharedViewModel.convertCoordsToNumber(it)
            when (sharedViewModel.minefieldWithNumbers[it[1]][it[0]]) {
                MINE.mark -> {
                    mineSelectedOnEmptySwitch = true
                    onMineSelected()
                    mineSelectedOnEmptySwitch = false
                }
                CLEARED.mark -> setEmptyBackground(id)
                ONE.alphaNumber -> setNumberBackgroundAndImage(ONE.number, id)
                TWO.alphaNumber -> setNumberBackgroundAndImage(TWO.number, id)
                THREE.alphaNumber -> setNumberBackgroundAndImage(THREE.number, id)
                FOUR.alphaNumber -> setNumberBackgroundAndImage(FOUR.number, id)
                FIVE.alphaNumber -> setNumberBackgroundAndImage(FIVE.number, id)
                SIX.alphaNumber -> setNumberBackgroundAndImage(SIX.number, id)
                SEVEN.alphaNumber -> setNumberBackgroundAndImage(SEVEN.number, id)
                EIGHT.alphaNumber -> setNumberBackgroundAndImage(EIGHT.number, id)
            }
        }

        if (sharedViewModel.listOfSelections.distinct().size + sharedViewModel.mineCounter.value == sharedViewModel.height.value * sharedViewModel.width.value) {
            if (checkIfWinMessageHasAlreadyAppeared) return else checkIfWinMessageHasAlreadyAppeared = true
            gameOverMessage(R.string.win)
        }
    }

    private fun setNumberBackgroundAndImage(image: Int, card: Int) {
        val cardView = createCardView(card, background = true, clickable = false)
        val imageView = createImageView(image)
        // all of these image views will have their ID the same as the cardView + 7500
        imageView.id = cardView.id + 7500
        cardView.addView(imageView)
        cardView.removeView(requireView().findViewById(card + 5000))
        checkAroundTheFlags()
    }

    private fun setEmptyBackground(card: Int) {
        val cardView = createCardView(card, background = true, clickable = false)
        cardView.removeView(requireView().findViewById(sharedViewModel.selectedCardId.value + 5000))
    }

    private fun createCardView(card: Int, background: Boolean, clickable: Boolean): CardView {
        val cardView = requireActivity().findViewById<CardView>(card)
        if (background) cardView.setBackgroundColor(getColor(requireContext(), sharedViewModel.getCardBackgroundColor(card,
            R.color.gray_400,
            R.color.gray_dark
        )))
        cardView.isClickable = clickable
        cardView.isLongClickable = clickable
        return cardView
    }

    private fun gameOverMessage(message: Int) {
        binding?.timeCounter?.stop()
        FinalMessageFragment.newInstance(getString(message), getString(R.string.time, binding?.timeCounter?.text))
            .show(childFragmentManager, FinalMessageFragment.TAG)
    }

    fun exitGame() = (0 until sharedViewModel.width.value * sharedViewModel.height.value)
        .forEach { makeClickable(sharedViewModel.convertNumberToCoords(it + 1), false) }

    fun clickTheFlag() {
        sharedViewModel.move(sharedViewModel.selectedCardId.value)
        buttonSelect()

        val cardView = requireActivity().findViewById<CardView>(sharedViewModel.selectedCardId.value)
        val possibleImageId = requireActivity().findViewById<ImageView>(sharedViewModel.getFlagId())
        val coords = sharedViewModel.currentCoords

        if (cardView.children.contains(possibleImageId)) removeFlag(cardView, possibleImageId, coords, coords[0], coords[1])
        else placeFlag(cardView, coords, coords[0], coords[1])
    }

    private fun placeFlag(cardView: CardView, coords: List<Int>, x: Int, y: Int) {
        sharedViewModel.addFlag(sharedViewModel.listOfFlags, coords, sharedViewModel.minefieldWithNumbers[y][x])

        val imageView = createImageView(R.drawable.flag_in_game)
        // flag ID will be card ID + 5000
        imageView.id = sharedViewModel.selectedCardId.value + 5000
        cardView.addView(imageView)
        checkAroundTheFlags()
        sharedViewModel.moveCounter.increment()
        resetBackgroundColorAfterSelection()
    }

    private fun removeFlag(cardView: CardView, imageView: ImageView, coords: List<Int>, x: Int, y: Int) {
        checkAroundRemovedFlags()
        sharedViewModel.removeFlag(sharedViewModel.listOfFlags, coords, sharedViewModel.minefieldWithNumbers[y][x])
        cardView.removeView(imageView)
        sharedViewModel.moveCounter.increment()
        resetBackgroundColorAfterSelection()
    }

    private fun resetBackgroundColorAfterSelection() {
        val findViewOfPreviouslySelected = requireActivity().findViewById<CardView>(sharedViewModel.selectedCardId.value)
        findViewOfPreviouslySelected.setBackgroundColor(getColor(requireContext(), sharedViewModel.refreshedBackgroundColor(
            sharedViewModel.selectedCardId.value, R.color.cyan_900, R.color.cyan_dark, R.color.gray_400, R.color.gray_dark
        )))
    }

    private fun createImageView(image: Int): ImageView {
        val imageView = ImageView(requireContext())
        imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imageView.setImageResource(image)
        return imageView
    }

    private fun checkAroundTheFlags() = sharedViewModel.listOfFlags.forEach { flag -> makeNumbersClickable(flag) }

    private fun makeNumbersClickable(flag: List<Int>) {
        (-1..1).forEach { i -> (-1..1).forEach j@{ j ->
            val xCurrent = flag[0] + j
            val yCurrent = flag[1] + i
            try { sharedViewModel.minefieldWithNumbers[yCurrent][xCurrent] } catch (e: ArrayIndexOutOfBoundsException) { return@j }

            val currentMineCoordsItem = sharedViewModel.minefieldWithNumbers[yCurrent][xCurrent]
            val currentMineCoords = listOf(xCurrent, yCurrent)

            when {
                i == 0 && j == 0 -> return@j
                !sharedViewModel.listOfNumbers.contains(currentMineCoordsItem) -> return@j
                !sharedViewModel.listOfSelections.contains(currentMineCoords) -> return@j
                sharedViewModel.listOfFlags.contains(currentMineCoords) -> {
                    makeClickable(currentMineCoords, true)
                    return@j
                }
            }

            if (currentMineCoordsItem.toInt() == sharedViewModel.countProximityMines(currentMineCoords)) makeClickable(currentMineCoords, true)
            else makeClickable(currentMineCoords, false)
        }}
    }

    private fun checkAroundRemovedFlags() {
        (-1..1).forEach { i -> (-1..1).forEach j@{ j ->
            val xCurrent = sharedViewModel.currentCoords[0] + j
            val yCurrent = sharedViewModel.currentCoords[1] + i
            try { sharedViewModel.minefieldWithNumbers[yCurrent][xCurrent] } catch (e: ArrayIndexOutOfBoundsException) { return@j }

            val currentMineCoordsItem = sharedViewModel.minefieldWithNumbers[yCurrent][xCurrent]
            val currentMineCoords = listOf(xCurrent, yCurrent)

            when {
                i == 0 && j == 0 -> return@j
                !sharedViewModel.listOfSelections.contains(currentMineCoords) -> return@j
                sharedViewModel.listOfFlags.contains(currentMineCoords) -> {
                    makeClickable(currentMineCoords, true)
                    return@j
                }
            }

            sharedViewModel.publicRemoveAll(sharedViewModel.currentCoords)
            try {
                if (currentMineCoordsItem.toInt() == sharedViewModel.countProximityMines(currentMineCoords)) makeClickable(currentMineCoords, true)
                else makeClickable(currentMineCoords, false)
            } catch (e: NumberFormatException) {}
        }}
    }

    private fun makeClickable(coords: List<Int>, trueOrFalse: Boolean) =
        createCardView(sharedViewModel.convertCoordsToNumber(coords), background = false, clickable = trueOrFalse)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.new_game_menu, menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_game_button -> {
                NewGameFragment.newInstance(getString(R.string.new_game))
                    .show(childFragmentManager, NewGameFragment.TAG)
                true
            }
            R.id.sign_out -> {
                sharedViewModel.user.changeValue(false)
                database.child("userLog").setValue(sharedViewModel.user.value)
                findNavController().navigate(R.id.action_minesweeperFragment_to_loginFragment)
                true
            }
            R.id.settings -> {
                SettingsFragment.newInstance(getString(R.string.settings))
                    .show(childFragmentManager, SettingsFragment.TAG)
                true
            }
            R.id.results -> {
                ResultsFragment.newInstance(sharedViewModel.usernameFromDB.value.uppercase())
                    .show(childFragmentManager, ResultsFragment.TAG)
                true
            }
            R.id.leader_board -> {
                LeaderBoardFragment.newInstance().show(childFragmentManager, LeaderBoardFragment.TAG)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeMineCounter() {
        sharedViewModel.mineCounterUI.dataValue.observe(viewLifecycleOwner) { binding?.mineCounter?.text = it.toString() }
        sharedViewModel.difficultySet.dataValue.observe(viewLifecycleOwner) { checkDifficulty() }
    }

    private fun restartGame(height: Int, width: Int, numberOfMines: Int) {
        val timeCounter = requireActivity().findViewById<Chronometer>(R.id.time_counter)
        timeCounter.stop()
        val parentView = requireActivity().findViewById<LinearLayout>(R.id.mine_lay)
        parentView!!.removeAllViews()
        sharedViewModel.resetGame(height, width, numberOfMines)
        buttonSelect()
        startGame(requireView())
        timeCounter.base = SystemClock.elapsedRealtime()
    }

    fun checkDifficulty() {
        when (sharedViewModel.difficultySet.dataValue.value.toString()) {
            EASY.difficulty -> restartGame(10, 10, 10)
            MEDIUM.difficulty -> restartGame(15, 13, 40)
            HARD.difficulty -> restartGame(27, 14, 75)
            EXPERT.difficulty -> restartGame(30, 16, 99)
            CUSTOM.difficulty -> restartGame(sharedViewModel.height.value, sharedViewModel.width.value, sharedViewModel.howManyMines.value)
        }
    }

    fun restartGameButtonClickListener() {
        countTheLoss(false)
        checkDifficulty()
    }

    override fun onDestroyView() {
        countTheLoss(false)
        super.onDestroyView()
    }

    fun countTheLoss(message: Boolean) {
        if (sharedViewModel.difficultySet.dataValue.value == CUSTOM.difficulty) return
        if (sharedViewModel.firstMoveSwitch.value != 0) {
            val timeCounter = requireView().findViewById<Chronometer>(R.id.time_counter)
            val time = SystemClock.elapsedRealtime() - timeCounter?.base!!
            sharedViewModel.updateComplexities(time, message)
            database.child(sharedViewModel.difficultySet.dataValue.value!!).setValue(sharedViewModel.getComplexity()[0])
        }
    }
}