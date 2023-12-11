# Prerequisites
1. Create an API key by following this instruction: https://developers.google.com/maps/documentation/android-sdk/get-api-key
2. Restrict API keys to the following APIs:

   Directions API, Maps SDK for Android, Maps SDK for iOS, Places API.

3. Clone this project using Android Studio and open `local.properties` file. Then append this line of code at the end:
   ```
   MAPS_API_KEY={YOUR_API_KEY}
   ```
   where `{YOUR_API_KEY}` is your generated API key.
