# MusicBox

Агрегатор музыки: YouTube Music + Яндекс Музыка + ВК Музыка в одном плеере.
Спотифай сознательно выкинут (там нет стрима без librespot + Premium).

## Что уже есть (скелет)

- Kotlin + Jetpack Compose, Material 3, тёмная тема
- Media3 (ExoPlayer) в зависимостях — шторка/локскрин будут штатные
- Единый контракт провайдера: `core/provider/MusicProvider.kt`
- Единая модель трека: `core/model/Track.kt`
- Экран-заглушка с поиском, списком сервисов и мини-плеером

## Сборка

APK собирается на GitHub Actions (`.github/workflows/build.yml`), локально
Android SDK на телефоне нет. Залить репо на GitHub — APK появится в артефактах
и в разделе Releases, если добавить шаг.

## Порядок работ

1. MediaSessionService + реальное воспроизведение (сначала локальные файлы)
2. YouTube Music через NewPipeExtractor
3. Яндекс Музыка (OAuth-токен)
4. ВК Музыка (user token, scope audio)
