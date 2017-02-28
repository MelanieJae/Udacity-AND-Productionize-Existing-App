The intent of this project is to incorporate fictional UX feedback from users to improve accessibility features and eliminate bugs. This project has as its base an existing Gradle Android Studio project that can be found here:

https://www.udacity.com/course/android-developer-nanodegree-by-google--nd801.

# App Notes

This app contains three screens and a widget that is accessible from the home and lock screens. The main screen shows a default list of stocks that can be added to by clicking the '+' button. That button opens a dialog (second screen) where the user's favorite stock symbol can be typed in and added to the list. Upon clicking on any of the stocks on the list you arrive at a third detail screen which shows a two-year-long graphical history of the price for the selected stock. The app also contains a widget which can be added in the usual manner from the home screen and is accessible from both the home and lock screens. Its size is customizable and it shows all of the stock information that can be found on the main screen of the app.

# Development Notes

## External Libraries:

MPAndroidChart,
YahooFinance

## Build System

This project and its base project are built using the Gradle build system.

The accessibility features added are as follows:

Arabic language support(region: Egypt) for all screens and most text
TalkBack support on all clickable items
Widget that can be added showing all of the user's favorite stocks and all information for those stocks that is available on the main activity screen


