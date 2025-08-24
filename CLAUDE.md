# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

This is an Android Sudoku game built with Kotlin, Jetpack Compose, and MVVM architecture. The app
features a complete sudoku experience with difficulty levels, timer, mistake tracking, note-taking
functionality, and comprehensive testing.

## Essential Commands

### Build & Run

```bash
# Build the project
./gradlew build

# Run debug build on device/emulator
./gradlew installDebug

# Clean build
./gradlew clean
```

### Testing

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.sandro.new_sudoku.SudokuViewModelTest"

# Run specific test method
./gradlew test --tests "com.sandro.new_sudoku.SudokuViewModelTest.초기 상태가 올바르게 설정되는지 테스트"

# Run UI tests (requires emulator/device)
./gradlew connectedDebugAndroidTest

# Generate test coverage reports
./gradlew jacocoTestReport
./gradlew jacocoUiTestReport
./gradlew jacocoCombinedReport
```

### Development

```bash
# Generate missing tests (custom script)
python3 generate-missing-tests.py

# Run all tests (unit + UI)
./gradlew testAll

# Generate UI test report
./gradlew uiTestReport
```

## Architecture Overview

### Core Classes Structure

- **MainActivity.kt**: Entry point with navigation between main/game screens
- **SudokuViewModel.kt**: Game state management (771 lines) - handles board state, timer, undo/redo,
  notes, mistakes
- **SudokuGame.kt**: Game logic engine - puzzle generation, validation, solutions
- **SudokuScreen.kt**: Main game UI composition
- **ui/MainScreen.kt**: Main menu with difficulty selection

### Key Design Patterns

- **MVVM**: Clean separation between UI (Compose) and business logic (ViewModel)
- **StateFlow**: Reactive state management for UI updates
- **Repository Pattern**: SudokuGame serves as data layer
- **Command Pattern**: Undo/Redo implemented with action stacks

### State Management

The `SudokuState` data class contains:

- `board`: 9x9 game grid
- `isInitialCells`: tracks puzzle-provided vs user-entered cells
- `selectedRow/Col`: current cell selection
- `invalidCells`: cells violating sudoku rules (displayed in red)
- `notes`: 3x3 candidate numbers per cell
- `mistakeCount`: tracks user errors (max 3)
- `timer fields`: elapsed time and running state
- `highlight fields`: visual feedback for selected numbers

### Testing Architecture

- **89 total tests**: 50 unit tests + 39 UI tests
- **SudokuViewModelTest.kt**: Comprehensive ViewModel testing (needs refactoring - 1,367 lines)
- **UI tests**: Espresso-based integration tests
- **Test mode**: `isTestMode` flag disables timer in tests

## Key Requirements & Constraints

### Core Functionality (from 요구사항.md)

1. **Number Input**: Numbers display immediately, invalid entries show in red
2. **Undo System**: Complete state restoration (board + UI state)
3. **Note Mode**: 3x3 grid of candidate numbers in cells, toggles with main numbers
4. **Initial Cell Protection**: Puzzle-provided numbers can be modified (unlike typical sudoku)

### Development Practices

- **Test-First Development**: Write tests before implementing features
- **Fast Tests Preferred**: Optimize for speed over complex scenarios
- **Avoid Flaky Tests**: Use deterministic approaches over random/timing-dependent tests
- **Update Documentation**: Modify `테스트_목록.md` when adding/changing tests

## Important Files

- `요구사항.md`: Core feature requirements
- `작업목록.md`: Feature roadmap (completed items struck through)
- `테스트_목록.md`: Comprehensive test documentation (89 tests cataloged)
- `run-ui-tests.sh`: Automated UI test runner with emulator checks

## Current State

- Core sudoku functionality complete
- Comprehensive test suite (89 tests passing)
- Outstanding features: persistent save/load, hints, design improvements
- All major requirements implemented and tested

## Development Notes

- Use `viewModel.isTestMode = true` in tests to disable timer
- UI tests require emulator/device connection
- Test coverage reports available via JaCoCo tasks
- Korean comments and test names are intentional and should be preserved

- UI 테스트는 ./gradlew connectedDebugAndroidTest로 실행한다.