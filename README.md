# XuluPort

Port de **Xulu v1.5.2** (Minecraft 1.12.2) a **Minecraft 1.18.2 / Forge**.

- **Versión:** `1.0.0-alpha`
- **Minecraft:** 1.18.2
- **Forge:** 40.2.17 o superior
- **Java:** 17
- **Estado:** build funcional; pendiente la validación funcional en juego por parte del dueño del proyecto.

Este repositorio contiene únicamente el **port** a 1.18.2. El código original decompilado de 1.12.2 se usa como referencia de comportamiento y **no se distribuye aquí**.

---

## Requisitos

- JDK 17 (el `build.gradle` fija toolchain 17).
- No hace falta instalar Gradle: se incluye el wrapper.
- El `gradle.properties` **no** debe llevar `org.gradle.java.home` (ruta local); se eliminó para que compile en cualquier máquina.

## Compilar

```bash
./gradlew build
```

El jar queda en:

```
build/libs/xuluport-1.0.0-alpha.jar
```

Para desarrollo:

```bash
./gradlew runClient
```

## Instalar

Copiar `build/libs/xuluport-1.0.0-alpha.jar` a la carpeta `mods/` de una instalación Forge 1.18.2.

---

## Estructura del proyecto

```
src/main/java/
  com/elementars/eclient/        # cliente (módulos, HUD, eventos, mixins, utilidades)
  dev/xulu/                      # settings, ClickGUI y NewGUI
src/main/resources/
  META-INF/mods.toml
  xulu.mixins.json
  assets/xulu/shaders/           # shaders del TitleScreen
```

---

## Contenido porteado

### Core / infraestructura

- `Xulu` (entrypoint, managers, fonts, HUD).
- `Module`, `ModuleManager`, `Category`, `Helper`.
- `ValueManager`, `Value<T>`, `Bind`, `TextBox`, `FileManager`, `OnChangedValue`.
- ClickGUI (`dev.xulu.clickgui`), NewGUI (`dev.xulu.newgui`), HUD editor (`guirewrite`).
- Font system (XFontRenderer, CFontManager, rainbow), utilidades (BlockInteractionHelper, EntityUtil, XuluTessellator, ColorUtils, etc.).
- Friends / Enemies, TargetPlayers, LagUtil, TaskScheduler.

### Módulos (41 registrados)

**Core (3)**
- `Global`, `CustomFont`, `TitleScreenShader`

**Combat (13)**
- `AutoArmor`, `AutoCrystal`, `AutoTotem`, `AutoTrap`, `Criticals`, `EXPFast`, `FastBow`, `HoleBlocker`, `HoleFill`, `Offhand`, `PearlPhase`, `PopCounter`, `Surround`

**Movement (7)**
- `ElytraFly`, `FastSwim`, `Flight`, `GuiMove`, `NoSlowDown`, `Sprint`, `Velocity`

**Player (3)**
- `AntiVoid`, `NoBreakAnimation`, `XCarry`

**Render (4)**
- `Chams`, `ExeterGui`, `FullBright`, `Skeleton`

**Misc (3)**
- `FakeVanilla`, `FovSlider`, `Time`

**HUD (8)**
- `HudEditor`
- Elementos: `PvPInfo`, `TextRadar`, `FeatureList`, `Position`, `Watermark`, `Potions`, `Totems`

### Mixins (17 en `xulu.mixins.json`)

`TitleScreenMixin`, `MixinConnection`, `MixinEntity`, `MixinFontRenderer`, `MixinGui`,
`MixinLocalPlayer`, `MixinContainerPlayer`, `MixinC00Handshake`, `MixinMinecraft`,
`MixinMinecraftStop`, `MixinMultiPlayerGameMode`, `MixinLivingEntityRenderer`,
`MixinPlayerRenderer`, `MixinEndCrystalRenderer`, `MixinModelPlayer`, `MixinKeyboardInput`,
`AccessorServerboundMovePlayerPacket`.

### Eventos propios (16)

`CloseInventoryEvent`, `EntityEvent`, `EventAttackEntity`, `EventKey`, `EventModelPlayerRender`,
`EventModelRender`, `EventPostRenderLayers`, `EventReceivePacket`, `EventSendPacket`,
`EventTotemPop`, `EventUpdate`, `LocalPlayerUpdateEvent`, `MotionEvent`, `MotionEventPost`,
`PlayerMoveEvent`, `RenderEvent`.

### Comandos (4)

`FriendCommand`, `EnemyCommand`, `FakePlayerCommand`, `AntiVoidCommand`.

---

## Cambios respecto al SRC original (1.12.2 → 1.18.2)

### Adaptaciones de API

- Migración completa a mappings `official`, Java 17, Forge 1.18.2 y Mixin `JAVA_17` con refmap `xulu.refmap.json`.
- Render migrado a `PoseStack` / `RenderLevelLastEvent` / `XuluTessellator` de 1.18.2.
- Input migrado a `Input`/`InputConstants` (campos `forwardImpulse`, `leftImpulse`, `jumping`).
- `MixinMinecraft.setRightClickDelay(int)` reemplaza al `rightClickDelayTimer` de 1.12.2.
- Items separados por metadata adaptados:
  - **Gapple**: `Items.GOLDEN_APPLE` + `Items.ENCHANTED_GOLDEN_APPLE` (Offhand, AutoCrystal).
  - **Netherite sword** cuenta donde el original chequeaba diamond sword (`Gap On Sword` en Offhand).
- `mc.timer.tickLength` (1.12.2) no tiene equivalente directo en el `Timer` de 1.18.2; se documenta como limitación en `ElytraFly.onDisable`.

### Correcciones y mejoras

- **GuiMove**: con GUI abierta, Forge marca los keybinds de movimiento como `IN_GAME` y `KeyMapping.isDown()` queda en `false`. Se agregó `MixinKeyboardInput` que, al final de `KeyboardInput.tick`, recalcula el input con `InputConstants.isKeyDown` si GuiMove está activo, hay una pantalla abierta y no es el chat.
- **ElytraFly**: `BYPASS` + `Type = 2` era una rama muerta en el original (`onUpdate` solo corría con `devMode == 1`). En el port `Type 2` usa la misma lógica que `Type 1`.
- **AutoTrap**:
  - `Expand` (default ON): si el objetivo no está centrado en un bloque, calcula las columnas que intersecta su hitbox y construye la jaula alrededor de todas.
  - `SelfDisable` (default ON): desactiva el módulo cuando todos los bloques del patrón quedaron sólidos.
  - Fix: al no encontrar obsidiana en la hotbar ya no se asigna `selected = -1` (se retorna tras `disable()`).
- **Surround**:
  - `Expand` (default ON): mismo criterio de expansión por hitbox.
  - `SelfDisable` (default ON): desactiva solo cuando el surround del modo actual está completo.
  - `DisableOnMove` (default ON): desactiva si el jugador se mueve en X/Z (epsilon 1e-4).
  - `DisableOnYChange` (default ON): el chequeo de cambio de Y del original ahora es configurable.
  - Reset de `offsetStep`/`delayStep`/`totalTicksRunning` al activar.
  - Fix: al no encontrar obsidiana en la hotbar ya no se asigna `selected = -1`.
- **HUD editor**:
  - La rueda ya **no desplaza los frames** (no pueden irse hacia arriba); el panel `Elements` queda limitado a `y >= 0`.
  - Al activar un HUD element que sigue en su posición de spawn, el frame aparece **centrado** en pantalla. Si ya tenía una posición guardada/arrastrada, se respeta.
  - Líneas de ejes vertical/horizontal en el editor.
- **FeatureList**: nuevo setting `Auto Align` (default ON). Decide alineación derecha/izquierda y crecimiento hacia arriba/abajo según la posición del frame; si está OFF mandan `Aligned` y `Ordering`.
- **Watermark / Potions / Totems**:
  - `Watermark` recalcula tamaño con la fuente custom y soporta `Rainbow`.
  - `Potions` usa `MobEffect#getDisplayName()` para el nombre y llama a `super.onEnable()`.
  - `Totems` cuenta también el totem de la offhand.

### Persistencia

- Se mantiene `Xulu/HUD.txt` para posiciones de HUD (y `Friends.txt` / `Enemies.txt`).
- **Limitación conocida:** todavía no existe guardado/carga de settings (`Value`) ni del estado toggled de los módulos; al reiniciar se pierden. Es uno de los pendientes para salir de alpha.

### No portado todavía

- Combat: 18 módulos (Aura, AutoLog, AutoWeb, MiddleClickPearl, SelfWeb, etc.).
- Movement: 8 (Step, Strafe, LongJump, PacketFly, Scaffold, etc.).
- Player: 11 (TpsSync, FastFall, AutoWalk, Freecam, etc.).
- Render: 36 (ESP suite, Xray, NoRender, Nametags, etc.).
- Misc: 25 (Timer, AntiSpam, CameraClip, etc.).
- HUD: 18 elementos (Crystals, Gapples, Obsidian, Exp, Armor, Target, etc.).

---



---

## Licencia

Este port se distribuye bajo **GNU General Public License v3.0 only** (`GPL-3.0-only`). Ver [LICENSE](LICENSE).

Aviso importante: el proyecto original **Xulu v1.5.2** es propiedad de sus autores y su código decompilado figura como *All Rights Reserved*. Este repositorio no incluye ese código original. Publicar y redistribuir un port derivado requiere contar con permiso del titular de los derechos del original; la GPLv3 aplica al código de este port en la medida en que quien lo publica tenga derechos para licenciarlo así.

## Créditos

- **Elementars** y autores de Xulu por el cliente original.
- **Xdolf** por el font renderer.
- Comunidad de XuluPort por el trabajo de porteo a 1.18.2.
