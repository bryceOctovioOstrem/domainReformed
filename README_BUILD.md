# Building Domain Reformed Mod

This mod is a Starsector faction mod that adds the "Domain Reformed" faction to the game.

## Quick Build

### Windows (PowerShell)
```powershell
.\build.ps1
```

### Windows (Command Prompt)
```cmd
build.bat
```

### Manual Build
1. Create a ZIP file containing:
   - `mod_info.json` (at the root)
   - `data/` folder
   - `graphics/` folder
   - `sounds/` folder

2. Name it `domainReformed_v1.6.zip` (or current version)

## Installation

1. Extract the ZIP file to your Starsector `mods/` folder
2. The folder structure should be: `Starsector/mods/domainReformed/`
3. Launch Starsector and enable the mod in the mod list

## Optional: Compile Java Files

If you have Starsector installed and want to pre-compile the Java files for better performance:

```powershell
.\build.ps1 -StarsectorPath "C:\Program Files\Starsector" -CompileJava
```

**Note:** Starsector can load `.java` files directly and compile them on-the-fly, so pre-compilation is optional.

## File Structure

```
domainReformed/
├── mod_info.json          # Mod metadata
├── data/                   # Game data files
│   ├── scripts/           # Java mod code
│   ├── hulls/             # Ship hull definitions
│   ├── variants/          # Ship variants
│   ├── weapons/           # Weapon definitions
│   └── config/            # Configuration files
├── graphics/              # Image assets
└── sounds/                # Audio assets
```

## Requirements

- Starsector 0.98a or compatible version
- No dependencies (as specified in mod_info.json)

