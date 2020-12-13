package ren.imyan.theme


enum class Theme(
    val themeId: Int,
    val themeName: String,
    val themeRes: Int
) {
    Blue(
        themeId = 0,
        themeName = "Blue",
        themeRes = R.style.BlueAppTheme
    ),
    Red(
        themeId = 1,
        themeName = "Red",
        themeRes = R.style.RedAppTheme
    ),
    Purple(
        themeId = 2,
        themeName = "Purple",
        themeRes = R.style.PurpleAppTheme
    ),
    Indigo(
        themeId = 3,
        themeName = "Indigo",
        themeRes = R.style.IndigoAppTheme
    ),
    Teal(
        themeId = 4,
        themeName = "Teal",
        themeRes = R.style.TealAppTheme
    ),
    Green(
        themeId = 5,
        themeName = "Green",
        themeRes = R.style.GreenAppTheme
    ),
    Orange(
        themeId = 6,
        themeName = "Orange",
        themeRes = R.style.OrangeAppTheme
    ),Brown(
        themeId = 7,
        themeName = "Brown",
        themeRes = R.style.BrownAppTheme
    ),BlueGrey(
        themeId = 8,
        themeName = "BlueGrey",
        themeRes = R.style.BlueGreyAppTheme
    ),Yellow(
        themeId = 9,
        themeName = "Yellow",
        themeRes = R.style.YellowAppTheme
    ),White(
        themeId = 10,
        themeName = "White",
        themeRes = R.style.WhiteAppTheme
    ),Dark(
        themeId = 11,
        themeName = "Dark",
        themeRes = R.style.DarkAppTheme
    )
}