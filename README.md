# 🎵 JavaFX Music Player
A sleek and modern music player built using JavaFX with dynamic UI theming

JavaFX Music Player combines stunning visuals with powerful playback features, extracting album artwork to create an adaptive UI experience. This project is currently in active development.

## ✨ Features
* 🎨 **Stylish UI** - Rounded corners, semi-transparent backgrounds, and smooth animations
* 📚 **Music Library Management** - Load and display tracks from a local `music` directory
* 🔄 **Queue System** - Supports a default queue with planned custom queue functionality
* 🖼️ **Embedded Album Art** - Extracts and displays album artwork from audio files
* 🎧 **Audio Format Support** - Plays MP3, WAV, M4A, and FLAC (auto-conversion via FFmpeg)
* ⏱️ **Progress Tracking** - Displays current playback time and allows seeking
* 🌈 **Custom Themes** - Dynamically adjusts the UI color based on album art

## 📋 Prerequisites
* Java 17+
* JavaFX SDK
* JAudioTagger library (for metadata and artwork extraction)
* FFmpeg (optional, for M4A/FLAC conversion)

## 🛠️ Installation
* Clone this repository

```
git clone https://github.com/StewyDev65/OfflineMusic.git
cd OfflineMusic
```

* Compile and run the application

```
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.media MusicPlayerApp.java
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.media MusicPlayerApp
```

* Place your music files inside the `music` folder

## 🚀 Usage
Run the application:

```
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.media MusicPlayerApp
```

### Navigation Controls
Icon | Function
---- | --------
`♪` | Now Playing View
`≡` | Library View

### Playback Controls
Control | Function
------- | --------
`▶ / ⏸` | Play/Pause
`⏮ / ⏭` | Skip Backward/Forward
Progress Bar | Click and drag to seek

### Basic Operations
* **Play a track**: Double-click on a song in the track list
* **Queue management**: Right-click options (in development)
* **Volume control**: Adjust using the slider in the bottom panel

## ⚠️ Development Status
This project is currently in active development. Many features are still being implemented:

* Custom playlist creation
* Advanced queue management
* Audio visualizations
* Cloud synchronization
* Remote control functionality
* Lyrics display

⚠️ Note: Screenshots and demos will be added once the interface design is finalized.

## 🔮 Planned Improvements
* Create and manage custom playlists
* Add audio visualization effects
* Implement lyrics display and synchronization
* Add equalizer and audio effects
* Develop mobile companion app
* Include cloud library synchronization

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.

## 🙏 Acknowledgments
* JavaFX community for excellent documentation
* Open-source audio libraries that inspired this project
* Contributors who test and provide feedback

<div align="center">
  <sub>Built with ❤️ by [Samuel Stewart]</sub>
</div>