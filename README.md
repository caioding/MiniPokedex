# MiniPokedex - Android Application

MiniPokedex is a simple Android application built with Kotlin that consumes the PokeAPI v2 (https://pokeapi.co/docs/v2) to display information about Pokémon.

## Features

*   **Splash Screen:** An initial screen displaying the app's name briefly before navigating to the main list.
*   **Pokémon List Screen (`MainActivity`):
    *   Displays a list of Pokémon using `RecyclerView`.
    *   Each item shows the Pokémon's name, ID, and image (loaded from the API using Coil).
    *   **Search Functionality:** Filter Pokémon by name using a `SearchView`.
    *   **Filter by Type:** Filter Pokémon by their type (e.g., Fire, Water, Grass) using a dialog selection.
    *   **Filter by Generation:** Filter Pokémon by their generation (e.g., Gen I, Gen II) based on their ID range using a dialog selection.
    *   Clicking on a Pokémon opens the Detail Screen.
*   **Pokémon Detail Screen (`DetailActivity`):
    *   Displays detailed information about the selected Pokémon:
        *   Name and ID.
        *   Image (official artwork preferred).
        *   Types.
        *   Height and Weight.
        *   Base stats (HP, Attack, Defense, Special Attack, Special Defense, Speed).

## Technologies Used

*   **Kotlin:** Primary programming language.
*   **Android SDK:** For building the native Android application.
*   **Retrofit:** For type-safe HTTP calls to the PokeAPI.
*   **Gson Converter:** For parsing JSON responses from the API.
*   **Coil (Coroutine Image Loader):** For loading and displaying Pokémon images efficiently.
*   **AndroidX Libraries:**
    *   `AppCompat` for base activity and theme support.
    *   `ConstraintLayout` for flexible UI design.
    *   `RecyclerView` for displaying lists efficiently.
    *   `CardView` for list item presentation.
    *   `Lifecycle (lifecycleScope)` for managing coroutines tied to component lifecycles.
*   **ViewBinding:** To easily interact with views in layouts.

## Setup and Build

1.  **Clone the Repository (or ensure you have the project files).**
2.  **Open in Android Studio:** Open the project using Android Studio (latest stable version recommended).
3.  **Sync Gradle Files:** Android Studio should automatically sync the Gradle files and download the required dependencies. If not, trigger a manual sync (File > Sync Project with Gradle Files or the elephant icon in the toolbar).
4.  **Add App Icon (Manual Step if not already present):
    *   This project is configured to use `ic_pokeball_launcher.png` and `ic_pokeball_launcher_round.png` as its launcher icons (defined in `AndroidManifest.xml`).
    *   Ensure you have these icon files (or your preferred icons) in the respective `res/mipmap-*` density folders (e.g., `res/mipmap-mdpi`, `res/mipmap-hdpi`, etc.).
    *   You can use Android Studio's **Asset Studio** (Right-click `res` > New > Image Asset) to generate these adaptive and legacy launcher icons easily from a source image.
5.  **Connect a Device or Start an Emulator:**
    *   Ensure you have an Android device connected with USB Debugging enabled, or an Android Virtual Device (AVD) running from the Device Manager.
6.  **Run the Application:**
    *   Select your target device/emulator from the dropdown menu in the toolbar.
    *   Click the **Run 'app'** button (green play icon) or use the menu option `Run > Run 'app'`.

## API Used

*   **PokeAPI v2:** https://pokeapi.co/
    *   List Pokémon: `https://pokeapi.co/api/v2/pokemon`
    *   Pokémon Details: `https://pokeapi.co/api/v2/pokemon/{id_or_name}`
    *   List Types: `https://pokeapi.co/api/v2/type`
    *   Pokémon by Type: `https://pokeapi.co/api/v2/type/{type_name}`

This project demonstrates basic Android development concepts including UI creation, API consumption, data display in a list, and user interaction with filtering and navigation.
