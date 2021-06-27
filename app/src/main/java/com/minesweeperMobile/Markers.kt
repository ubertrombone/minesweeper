package com.minesweeperMobile

enum class Markers(val mark: String) {
    MINE("X"),
    FLAG("*"),
    EMPTY("."),
    CLEARED("/"),
}

enum class Numbers(val number: Int, val alphaNumber: String) {
    ONE(R.drawable.one, "1"),
    TWO(R.drawable.two, "2"),
    THREE(R.drawable.three, "3"),
    FOUR(R.drawable.four, "4"),
    FIVE(R.drawable.five, "5"),
    SIX(R.drawable.six, "6"),
    SEVEN(R.drawable.seven, "7"),
    EIGHT(R.drawable.eight, "8"),
}

enum class Difficulties(val difficulty: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    EXPERT("Expert"),
    CUSTOM("Custom")
}