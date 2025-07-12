# Auto brightness
## The app is replacement for system auto brightness  
The app can edit auto brightness levels and adjusts brightness only when screen becomes on or the app is started
## Description
The app shows a small dialog with a brightness slider and a 'Save' button to associate current light sensor value with selected brightness  
## Why not use system auto brightness?
System auto brightness runs continuously and the outside illumination changes rather frequently, so phone brightness level may change randomly causing discomfort. System illumination - brightness presets don't fit for all people, especially in dark conditions inside the room. So I decided to create the app with sets auto brightness only when phone screen is turned on with an ability to tune illumination - brightness levels       
## Preparation
1. Disable system auto brightness
2. Create a gesture for reliable opening the app (even if screen is dark). There are many gesture apps with custom app start support. I would recommend 
'Vivid Navigation Gestures'
3. Give the app permission for changing system settings (to change brightness level)
4. If you are disturbed with app persistent notification disable it in app system settings; persistent running service is needed to for tuning auto brightness level when screen turns on 
## Use cases
- When the phone screen is too dark or too bright start the app by a gesture, set desired brightness level and press **Save** button to associate selected level with current illumination   
- When the screen becomes turned on the app service would set brightness to auto level based on your previous Save commands
- You can see current relation between illumination and brightness by tap on corresponding button
## Analog apps
The only usable analog app known to me is Lux Auto Brightness, but is has not been updated for a long time and cause some problems on modern devices