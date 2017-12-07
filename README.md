# JOT: A Modular Multi-purpose Game Engine


JOT (i.e., minimal thing) modular multi-purpose game engine built in Java. 
JOT is intended to serve as a modular multi-purpose game engine for both academia, hobbists, and or industry. 

JOT was presented as a Videojogos (VJ 2016), Covilhã, 24 and 25 nov. conference article.

Presentation: https://goo.gl/i9eJFy

Article: https://goo.gl/sGBVQT

## <a name="toc">Table of contents 

* [JOT Windows 7-10 Setup](#windows_setup)
* [JOT Linux Setup](#linux_setup)
* [JOT Demos/Templates](#jot_demos)
* [JOT Architecture](#jot_architecture)
* [Project Organization](#p_organization)
* [Contact](#contact)


## <a name="windows_setup">JOT Windows 7-10 Setup

###Step 1: install netbeans for windows

For additional instructions please https://netbeans.org/community/releases/82/install.html

###Step 2: install tortoisegit on windows

https://tortoisegit.org/docs/tortoisegit/tgit-intro-install.html

###Step 3: Clone JOT repository in tortoisegit

-In windows explorer inside the intended clonning destination folder (recommended C:\), 
mouse right click and in the explorer context menu and select Git Clone. 

-In the field 'URL' paste 'https://github.com/g-amador/JOT.git' and click the 'Ok' button 

-Detailed docummentation available at: https://tortoisegit.org/docs/tortoisegit/tgit-dug-clone.html


###Step 4: Add JogAmp env. variable to windows (only required if step 5 does not work!!!!!)

-Add an environment variable to the location of the jogamp operative system libraries, e.g., in the 'Path' 'System Variable' 
add 'C:\JOT\lib\jogamp-all-platforms\lib' (assuming C:\ was where JOT was clonned).

-Instructions on how to add an environment variable to windows (7-10) are available at:
https://www.opentechguides.com/how-to/article/windows-10/113/windows-10-set-path.html

###Step 5: Run demos or templates

-Open either the template in the templates folder or either demo on the 'demos' folder in netbeans.

-Build and set as main project.

-Run demo.

-Options for either template/demo are in the netbeans corresponding demo/template 'assets/scripts/GameConstants.js', 
this file can be changed bettween runs without require recommpiling of the project.

[Back to Table of contents](#toc)


## <a name="linux_setup">JOT Linux Setup

(UNDER CONSTRUCTION.)


###Step 5: Run demos or templates

-Open either the template in the templates folder or either demo on the 'demos' folder in netbeans.

-Build and set as main project.

-Run demo.

-Options for either template/demo are in the netbeans corresponding demo/template 'assets/scripts/GameConstants.js', 
this file can be changed bettween runs without require recommpiling of the project.

[Back to Table of contents](#toc)


## <a name="jot_demos">JOT Demos/Templates

JOT provides as is 4 working netbeans example project demos, each supporting movement, model loading, direct rendering, FSP and help text, Keyboard and mouse I/O:
 
-Core template: a minimal example made solely with the Core layer, also includes 3D sound. 
-Game World Demo: World/terrain generation demo build resorting to the framework, tookits and core layers, also includes camera views.
-Physics Demo: Collision detection and light effects demo build resorting to the framework, tookits and core layers.
-AI Demo: Steering behaviours and pathfinding demo build resorting to the framework, tookits and core layers.

(other templates/demos exist but are currently broken thus will be available sometime in the future)./home/gpaiva/Desktop/projects/code/git/GitHub/assets/toolkits/AI/AI1.png

| Core Template | JOT Game World Demo | JOT Physics Demo |JOT AI Demo |
|:---:|:---:|:---:|:---:|
| [![Core Template](https://raw.githubusercontent.com/g-amador/JOT/assets/images/core/core.png)](https://raw.githubusercontent.com/g-amador/JOT/assets/images/core/core.png) | 
| [![Game World Demo](https://raw.githubusercontent.com/g-amador/JOT/assets/images/framework/FPScamera1.png)](ttps://raw.githubusercontent.com/g-amador/JOT/assets/images/framework/FPScamera1.png) |
| [![Physics Demo](https://raw.githubusercontent.com/g-amador/JOT/assets/images/framework/shadows.png)](ttps://raw.githubusercontent.com/g-amador/JOT/assets/images/framework/shadows.png) |
| [![AI Demo](https://raw.githubusercontent.com/g-amador/JOT/assets/images/toolkits/AI/shadows.png)](ttps://raw.githubusercontent.com/g-amador/JOT/assets/images/toolkits/AI/AI1.png) |

[Back to Table of contents](#toc)


## <a name="jot_architecture">JOT Architecture

JOT is a game engine divided into layers, from bottom to top: Infrastructure, Core, Toolkits, and Framework (see Fig. 1).

| [![Fig. 1](https://raw.githubusercontent.com/g-amador/JOT/assets/images/JOT-ARCH.png)](https://raw.githubusercontent.com/g-amador/JOT/assets/images/JOT-ARCH.png) | 


Any layer component that resorts to an external library obeys to interface/abstract classes as expected by above layers.
Each layer constituents can only use functionalities of the same layer or lower layers constituents.
In order to replace a component in the engine, one might solely adapt the respective toolkit or additionally modify its lower layers.


### Infrastructure Layer
These are the libraries\frameworks in which JOT layers and components were built on.

### Core Layer
The minimal set of tools that can allow the classification of a piece of software as a game engine.

### Toolkits Layer

This layer includes toolkits, which are extensions to the core.
There are toolkits for a number of purposes, namely artificial intelligence (AI),  geometry generators, physics simulation, etc.
Each toolkit includes an \emph{Utils} component which specifies its options (boolean values). 

### Framework Layer

This is the  upper layer of JOT.
This layer aims at the following: first, to provide management of the application/game state and scene; second, to separate the game logic from its graphical application.

[Back to Table of contents](#toc)


## <a name="p_organization">Project Organization


    .
    ├── assets                                          # Images, music, sound, effects, HOG2 maps, textures, required for the demos to run. 
    ├── demos                                           # Netbeans projects for working demos.
    ├── docs                                            # Documentation files
    ├──── incremental-fluids                            # Original C++ source code of fluid simulators (undergoing integration into JOT).
    ├──── javadoc                                       # JOT javadoc.
    ├──── presentation                                  # JOT Videojogos (VJ 2016), Covilhã, 24 and 25 nov. conference article presentation.
    ├──── smallPT1.ppt.pdf                              # Java RayTracer (undergoing integration into JOT).
    ├──── TS-3073.pdf  
    ├── engine                                          # Engine Maven project (requires build prior to either of its layers or components being build/rebuilt).
    ├──── Core-Toolkit-Components                       # Core layer Maven project.
    ├──── Extension-Toolkit-Components   
    ├────── AI                                          # Toolkits layer AI component Maven project.
    ├────── Communication                               # Toolkits layer Communication component Maven project.
    ├────── Geometry                                    # Toolkits layer Geometry component Maven project.
    ├────── Physics                                     # Toolkits layer physics component Maven project.
    ├──── Framework-Toolkit-Components                  # Framework layer Maven project.
    ├── lib                                             # Infrastructure libraries and engine by layer/component generated .jars and full engine .jar
    ├── templates                                       # Netbeans project for core layer template.
    ├── LICENSE
    └── README.md                          

[Back to Table of contents](#toc)


## <a name="contact">Contact

You are free to use and modify JOT as you see fit: to create your own multitouch apps, to extend it, and to port it to another programming language. 
You cannot take the credit for making JOT! 
Please inform me if you either extend JOT, create more templates/demos for/with JOT, or port it to another programming language.

If you have any questions, feel free to e-mail me at [gmail](mailto://g.n.p.amador@gmail.com) and ask away.

Good luck!

[Back to Table of contents](#toc)
