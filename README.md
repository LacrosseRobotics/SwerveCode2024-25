# Team 4054 - The Areonauts 
### Based on Yet Another Generic Swerve Library (YAGSL) Example project

# Overview

### Installation

Vendor URL:

```
https://broncbotz3481.github.io/YAGSL-Lib/yagsl/yagsl.json
```

[Javadocs here](https://broncbotz3481.github.io/YAGSL/)  
[Library here](https://github.com/BroncBotz3481/YAGSL/)  
[Code here](https://github.com/BroncBotz3481/YAGSL/tree/main/swervelib)  
[WIKI](https://github.com/BroncBotz3481/YAGSL/wiki)  
[Config Generation](https://broncbotz3481.github.io/YAGSL-Example/)

### TL;DR Generate and download your configuration [here](https://broncbotz3481.github.io/YAGSL-Example/) and unzip it so that it follows structure below:

```text
deploy
└── swerve
    ├── controllerproperties.json
    ├── modules
    │   ├── backleft.json
    │   ├── backright.json
    │   ├── frontleft.json
    │   ├── frontright.json
    │   ├── physicalproperties.json
    │   └── pidfproperties.json
    └── swervedrive.json
```

### Then create your SwerveDrive object like this.

```java
import java.io.File;
import edu.wpi.first.wpilibj.Filesystem;
import swervelib.parser.SwerveParser;
import swervelib.SwerveDrive;
import edu.wpi.first.math.util.Units;