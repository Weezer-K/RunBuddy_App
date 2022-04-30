# CS501-RunBuddy

Members: Kyle Peters, Paul Menexas, Zhaoguo Zhu

Before using our repository, make sure to send us your SHA1 key. We will then update our google.json file with your key so that you can run our app on your device.

## Important Notices:
* Make sure to have the run configuration set to "app" instead of "app.libs" or anything else.
* We calculate distance ran by using location data from every 5 seconds. Therefore don't run in place like on a treadmill. Instead you should run to make sure your locations are changing meaningfully. 
* If you sit still, the UI may still display slight increases in distance travelled. This is because GPS coordinates have a slight difference between one point and the next due to a margin of error. This could be acccumulated and captured in our distance calculation. We have a threshold that removes very low distances but it is possible for some values to be high enough to be interpreted as distance ran.
* If you choose to use a Mock GPS instead of really running to test it, follow the guidance below in "Mock GPS". If you choose to run for real

## Mock GPS:
* Step 1: Download The Mock Locations app off the google play store, Link: https://play.google.com/store/apps/details?id=ru.gavrikov.mocklocations
* Step 2: In "Settings" go to "Developer options", set "Select mock location app" as the gps mock app you just downloaded
* Step 3: In "Settings" go to "Location" and then in the "Location Services" section, go to "Google Location Accuracy". Make sure "Improve Location Accuracy" is turned off here.
* Step 4: Go to the Mock Locations app you downloaded. In the map, long click on the map to mark a “starting position” and then long click again on another point to set an “ending position”. Then click ✅ to continue. Then it will ask for some metrics like how fast you want to run etc. After setting up the details, click “go!” and then the fake run will start. Make sure to have this app running in the background when you use RunBuddy.
* Note: If you first test with the Mock Locations app and then want to use your real GPS, make sure to undo each of these steps. This means, go to "Developer options" and set "Select mock location app" to "Nothing". Then, in "Settings" go to "Location" and then in the "Location Services" section, go to "Google Location Accuracy". Make sure "Improve Location Accuracy" is turned on here. Lastly, make sure to kill the mock app if it is still running in the background.

## Using the App (Sign in):
* Select "Sign in with Fitbit" or "Sign in without Fitbit". For the first option, follow Step a, for the second option, follow Step b.
* Step a: Login with a valid Gmail Account. Then sign in with a fitbit account and grants access to the app to use your data. If you don't have a fitbit account, you can use ours. (Username: pmenexas13@gmail.com Password: 8NS#?v%vTUPkPfs) Go to step 2.
* Step b: Login with a valid Gmail Account and that's it.
* You are all set to go; Follow the final report pdf which serves as a walkthrough of the app


