package com.minesweepermobile.minesweeper

import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.minesweepermobile.model.MinesweeperViewModel
import com.minesweepermobile.databinding.FragmentMinesweeperBinding
import java.lang.ClassCastException
import com.minesweepermobile.Markers.*
import com.minesweepermobile.Numbers.*
import com.minesweepermobile.Difficulties.*
import com.minesweepermobile.newgame.NewGameFragment
import com.minesweepermobile.R
import com.minesweepermobile.settings.SettingsFragment
import com.minesweepermobile.database.Statistics
import com.minesweepermobile.finalmessage.FinalMessageFragment
import com.minesweepermobile.login.LoginFragment
import com.minesweepermobile.results.ResultsFragment
import java.lang.NullPointerException
import java.lang.NumberFormatException

class MinesweeperFragment: Fragment() {

    private var binding: FragmentMinesweeperBinding? = null
    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").reference

    private var shovelEmptySwitch = false
    private var mineSelectedOnEmptySwitch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()
        auth = Firebase.auth
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

        queryUserFromDatabase()
        observeUserState()
        startGame(view)
        observeMineCounter()
        restartGameButtonClickListener()
    }

    private fun queryUserFromDatabase() {
        database.addListenerForSingleValueEvent(readDatabase("user"))

        if (!sharedViewModel.startSwitch) {
            sharedViewModel.changeStartSwitch(true)
            return
        }
        val sharedValues = sharedViewModel.getIntersectionOfDatabaseAndComplexities()
        if (sharedValues.size != 4) setupDatabase(database) else database.addListenerForSingleValueEvent(readDatabase("complexities"))
    }

    private fun setupDatabase(database: DatabaseReference) {
        val allComplexities = Statistics(0, 0, 0, 0.0, 0L, 0L, 0, 0L, 0, 0, 0.0)
        sharedViewModel.changeAll(allComplexities)
        database.child(LoginFragment.userId).child(EASY.difficulty).setValue(sharedViewModel.easy[0])
        database.child(LoginFragment.userId).child(MEDIUM.difficulty).setValue(sharedViewModel.medium[0])
        database.child(LoginFragment.userId).child(HARD.difficulty).setValue(sharedViewModel.hard[0])
        database.child(LoginFragment.userId).child(EXPERT.difficulty).setValue(sharedViewModel.expert[0])
    }

    private fun readDatabase(reference: String): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (reference == "user") pickUpUserFromDatabase(dataSnapshot)
                else if (reference == "complexities") pickUpComplexitiesFromDatabase(dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) = println("FAIL")
        }
    }

    private fun pickUpUserFromDatabase(dataSnapshot: DataSnapshot) {
        dataSnapshot.children.forEach { child ->
            child.children.forEach { sharedViewModel.addKey(it.key.toString()) }
            if (child.child("userLog").key.toString() == "userLog") {
                sharedViewModel.getUser(child.child("userLog").value.toString().toBoolean())
                LoginFragment.userId = child.key.toString()
            }
        }
        dataSnapshot.children.forEach { child ->
            if (child.child("RTL").key.toString() == "RTL") {
                sharedViewModel.fabButtonSettings(child.child("RTL").value.toString().toBoolean())
                binding?.fabButtons?.layoutDirection = if (sharedViewModel.fabButtonRTL) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            }
        }
        dataSnapshot.children.forEach { child ->
            if (child.child("DefaultDifficulty").key.toString() == "DefaultDifficulty") {
                sharedViewModel.setDifficulty(child.child("DefaultDifficulty").value.toString())
                sharedViewModel.setDifficultyHolder(child.child("DefaultDifficulty").value.toString())
            }
        }
    }

    private fun pickUpComplexitiesFromDatabase(dataSnapshot: DataSnapshot) {
        dataSnapshot.children.forEach { child ->
            child.children.forEach {
                val complexityChildren = mutableListOf<String>()
                it.children.forEach { item -> complexityChildren.add(item.value.toString()) }
                when (it.key) {
                    EASY.difficulty -> sharedViewModel.changeEasy(getComplexityValue(complexityChildren))
                    MEDIUM.difficulty -> sharedViewModel.changeMedium(getComplexityValue(complexityChildren))
                    HARD.difficulty -> sharedViewModel.changeHard(getComplexityValue(complexityChildren))
                    EXPERT.difficulty -> sharedViewModel.changeExpert(getComplexityValue(complexityChildren))
                }
            }}
    }

    private fun getComplexityValue(value: MutableList<String>): Statistics {
        return Statistics(value[5].toInt(), value[6].toInt(), value[8].toInt(), value[0].toDouble(), value[9].toLong(),
            value[1].toLong(), value[4].toInt(), value[3].toLong(), value[2].toInt(), value[7].toInt(), value[10].toDouble())
    }

    private fun observeUserState() = sharedViewModel.user.observe(viewLifecycleOwner) {
        if (!it) findNavController().navigate(R.id.action_minesweeperFragment_to_loginFragment)
    }

    private fun startGame(view: View) {
        binding?.mineCounter?.text = sharedViewModel.howManyMines.toString()
        val mainLinearLayout = binding?.mineLay as LinearLayout
        (0 until sharedViewModel.height).forEach { i ->
            val rowLinearLayout = LinearLayout(requireContext())
            rowLinearLayout.orientation = LinearLayout.HORIZONTAL
            (0 until sharedViewModel.width).forEach { j ->
                val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.height = getParams(sharedViewModel.width)
                params.width = getParams(sharedViewModel.width)

                val cardView = setupUICardView(i, j, params)
                val cardLinearLayout = LinearLayout(requireContext())
                cardLinearLayout.orientation = LinearLayout.VERTICAL

                cardView.setOnClickListener {
                    sharedViewModelSetters(view, cardView)
                    getCoordsAndSetupClickListeners(view, cardView, true)
                }

                cardView.setOnLongClickListener {
                    sharedViewModelSetters(view, cardView)
                    getCoordsAndSetupClickListeners(view, cardView,false)
                    true
                }

                val imageView = setupUIImageView(params.height, cardView.id)
                cardLinearLayout.addView(imageView)
                cardView.addView(cardLinearLayout)
                rowLinearLayout.addView(cardView)
            }
            mainLinearLayout.addView(rowLinearLayout)
        }
    }

    private fun setupUICardView(i: Int, j: Int, params: ViewGroup.LayoutParams): CardView {
        val cardView = CardView(requireContext())
        cardView.setContentPadding(0, 0, 0, 0)
        cardView.id = if (i == 0) j else sharedViewModel.width * i + j
        cardView.setBackgroundColor(getColor(requireContext(), sharedViewModel.getCardBackgroundColor(cardView.id,
            R.color.cyan_900,
            R.color.cyan_dark
        )))
        cardView.layoutParams = params
        return cardView
    }

    private fun setupUIImageView(params: Int, cardViewId: Int): ImageView {
        val imageView = createImageView(boxSize(params))
        imageView.scaleType = ImageView.ScaleType.CENTER
        imageView.visibility = View.GONE
        // all of these image views will have their ID the same as the cardView + 2500
        imageView.id = cardViewId + 2500
        return imageView
    }

    private fun onClickSettings(flagAlpha: Float, flagEnable: Boolean, axeAlpha: Float, axeEnable: Boolean, shovelSwitch: Boolean) {
        fabFlagSettings(flagAlpha, flagEnable)
        fabAxeSettings(axeAlpha, axeEnable)
        shovelEmptySwitch = shovelSwitch
    }

    private fun sharedViewModelSetters(view: View, cardView: CardView) {
        try {
            val findViewOfPrevious = view.findViewById<ImageView>(sharedViewModel.selectedCellBackgroundId)
            findViewOfPrevious.visibility = View.GONE
        } catch (e: ClassCastException) {}

        sharedViewModel.getSelectedCardId(cardView.id)
        // ID is zeroed and needs to be offset by 1 when converting to coords
        sharedViewModel.getCurrentCoords(cardView.id + 1)
        sharedViewModel.getSelectedCellBackgroundId(cardView.id + 2500)
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
        val findViewOfCurrent = view.findViewById<ImageView>(sharedViewModel.selectedCellBackgroundId)
        findViewOfCurrent.visibility = View.VISIBLE
        val possibleImageId = requireActivity().findViewById<ImageView>(sharedViewModel.getFlagId())

        when {
            sharedViewModel.firstMoveSwitch == 0 -> clickTheShovel()
            sharedViewModel.listOfSelections.contains(coords) && !sharedViewModel.listOfFlags.contains(coords) && sharedViewModel.listOfNumbers.contains(coordsValue) -> {
                onClickSettings(.25F, false, 1F, true, shovelSwitch = true)
            }
            cardView.children.contains(possibleImageId) -> onClickSettings(1F, true, .25F, false, shovelSwitch = false)
            else -> onClickSettings(1F, true, 1F, true, shovelSwitch = false)
        }
    }

    private fun onLongClickListener(coords: List<Int>, coordsValue: String, cardView: CardView) {
        when {
            sharedViewModel.firstMoveSwitch == 0 -> clickTheShovel()
            sharedViewModel.listOfSelections.contains(coords) && !sharedViewModel.listOfFlags.contains(coords) && sharedViewModel.listOfNumbers.contains(coordsValue) -> {
                buttonSelect()
                onEmptySelected(true)
                sharedViewModel.incrementMoveCounter()
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

    private fun boxSize(height: Int) = when (height) {
        100 -> R.drawable.small_box_50
        76 -> R.drawable.small_box_38
        70 -> R.drawable.small_box_35
        else -> R.drawable.small_box_31
    }

    private fun buttonSelect() {
        fabFlagSettings(.25F, false)
        fabAxeSettings(.25F, false)
        try {
            requireActivity().findViewById<ImageView>(sharedViewModel.selectedCellBackgroundId).visibility = View.GONE
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
        sharedViewModel.incrementMoveCounter()

        try {
            val cardView = requireActivity().findViewById<CardView>(sharedViewModel.selectedCardId)
            val findViewOfPrevious = requireActivity().findViewById<ImageView>(2273546327529688.toInt())
            cardView.removeView(findViewOfPrevious)
        } catch (e: ClassCastException) {}

        if (sharedViewModel.firstMoveSwitch == 0) {
            binding?.timeCounter?.base = SystemClock.elapsedRealtime()
            binding?.timeCounter?.start()
        }
        sharedViewModel.move(sharedViewModel.selectedCardId)
        buttonSelect()

        if (shovelEmptySwitch) {
            onEmptySelected(true)
            shovelEmptySwitch = false
            return
        }

        sharedViewModel.addToSelections(sharedViewModel.currentCoords)
        when (sharedViewModel.getItemAtMinefieldPosition()) {
            MINE.mark -> onMineSelected()
            EMPTY.mark -> onEmptySelected(false)
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
        createCardAndImage(image, sharedViewModel.selectedCardId, 7500, true)
        if (sharedViewModel.listOfSelections.distinct().size + sharedViewModel.mineCounter == sharedViewModel.height * sharedViewModel.width) {
            if (numberOrNot) gameOverMessage(R.string.win)
        }
    }

    // Mine Image: By KDE Kmines team: [1] - gnomine artwork., GPL, https://commons.wikimedia.org/w/index.php?curid=2102166
    private fun onMineSelected() {
        val mines = sharedViewModel.mineLocations

        if (!mineSelectedOnEmptySwitch) {
            addImageView(R.drawable.gnome_gnomine__2_, false)
            mines.remove(sharedViewModel.selectedCardId)
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

    private fun onEmptySelected(number: Boolean) {
        sharedViewModel.clearEmptySelections()
        val coords = sharedViewModel.currentCoords
        sharedViewModel.emptyCells(coords[1], coords[0], sharedViewModel.minefieldWithNumbers, number)

        sharedViewModel.emptySelections.forEach {
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

        if (sharedViewModel.listOfSelections.distinct().size + sharedViewModel.mineCounter == sharedViewModel.height * sharedViewModel.width) {
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
        cardView.removeView(requireView().findViewById(sharedViewModel.selectedCardId + 5000))
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
        val supportFragmentManager = childFragmentManager
        FinalMessageFragment.newInstance(getString(message), getString(R.string.time, binding?.timeCounter?.text))
            .show(supportFragmentManager, FinalMessageFragment.TAG)
    }

    fun exitGame() = (0 until sharedViewModel.width * sharedViewModel.height)
        .forEach { makeClickable(sharedViewModel.convertNumberToCoords(it + 1), false) }

    fun clickTheFlag() {
        sharedViewModel.move(sharedViewModel.selectedCardId)
        buttonSelect()

        val cardView = requireActivity().findViewById<CardView>(sharedViewModel.selectedCardId)
        val possibleImageId = requireActivity().findViewById<ImageView>(sharedViewModel.getFlagId())
        val coords = sharedViewModel.currentCoords

        if (cardView.children.contains(possibleImageId)) removeFlag(cardView, possibleImageId, coords, coords[0], coords[1])
        else placeFlag(cardView, coords, coords[0], coords[1])
    }

    private fun placeFlag(cardView: CardView, coords: List<Int>, x: Int, y: Int) {
        sharedViewModel.addFlag(sharedViewModel.listOfFlags, coords, sharedViewModel.minefieldWithNumbers[y][x])

        val imageView = createImageView(R.drawable.flag_in_game)
        // flag ID will be card ID + 5000
        imageView.id = sharedViewModel.selectedCardId + 5000
        cardView.addView(imageView)
        checkAroundTheFlags()
        sharedViewModel.incrementMoveCounter()
    }

    private fun removeFlag(cardView: CardView, imageView: ImageView, coords: List<Int>, x: Int, y: Int) {
        checkAroundRemovedFlags()
        sharedViewModel.removeFlag(sharedViewModel.listOfFlags, coords, sharedViewModel.minefieldWithNumbers[y][x])
        cardView.removeView(imageView)
        sharedViewModel.incrementMoveCounter()
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
        val supportFragmentManager = childFragmentManager
        return when (item.itemId) {
            R.id.new_game_button -> {
                NewGameFragment.newInstance(getString(R.string.new_game))
                    .show(supportFragmentManager, NewGameFragment.TAG)
                true
            }
            R.id.sign_out -> {
                sharedViewModel.getUser(false)
                database.child(LoginFragment.userId).child("userLog").setValue(sharedViewModel.user.value)
                true
            }
            R.id.settings -> {
                SettingsFragment.newInstance(getString(R.string.settings))
                    .show(supportFragmentManager, SettingsFragment.TAG)
                true
            }
            R.id.results -> {
                ResultsFragment.newInstance(getString(R.string.results))
                    .show(supportFragmentManager, ResultsFragment.TAG)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeMineCounter() {
        sharedViewModel.mineCounterUI.observe(viewLifecycleOwner) { binding?.mineCounter?.text = it.toString() }
        sharedViewModel.difficultySet.observe(viewLifecycleOwner) { checkDifficulty() }
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
        when (sharedViewModel.getDifficultySet()) {
            EASY.difficulty -> restartGame(10, 10, 10)
            MEDIUM.difficulty -> restartGame(15, 13, 40)
            HARD.difficulty -> restartGame(27, 14, 75)
            EXPERT.difficulty -> restartGame(30, 16, 99)
            CUSTOM.difficulty -> restartGame(sharedViewModel.height, sharedViewModel.width, sharedViewModel.howManyMines)
        }
    }

    fun restartGameButtonClickListener() = checkDifficulty()
}