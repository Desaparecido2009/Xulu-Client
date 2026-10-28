# XuluPort

Port de **Xulu v1.5.2** (Minecraft 1.12.2) a **Minecraft 1.18.2 / Forge**.

- **Versión:** `1.0.0-alpha`
- **Minecraft:** 1.18.2
- **Forge:** 40.2.17 o superior
- **Java:** 17
- **Estado:** build funcional; pendiente la validación funcional en juego por parte del dueño del proyecto.

Este proyecto contiene únicamente el **port** a 1.18.2 y se publica en [Desaparecido2009/Xulu-Client](https://github.com/Desaparecido2009/Xulu-Client). El código original decompilado de 1.12.2 se usa como referencia de comportamiento y **no se distribuye aquí**.

---

## Comunidad y changelog

El **changelog completo de esta versión** se publica en el Discord oficial del port: **https://discord.gg/uUxbA4RAaS**. Ahí también se anuncian builds, cambios y soporte.

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
- Font system (XFontRenderer, CFontManager, rainbow), utilidades (BlockInteractionHelper, EntityUtil, LagCompensator, XuluTessellator, ColorUtils, etc.).
- Friends / Enemies, TargetPlayers, LagUtil, TaskScheduler.

### Módulos (52 registrados)

**Core (3)**
- `Global`, `CustomFont`, `TitleScreenShader`

**Combat (15)**
- `Aura`, `AutoArmor`, `AutoCity`, `AutoCrystal`, `AutoTotem`, `AutoTrap`, `Criticals`, `EXPFast`, `FastBow`, `HoleBlocker`, `HoleFill`, `Offhand`, `PearlPhase`, `PopCounter`, `Surround`

**Movement (7)**
- `ElytraFly`, `FastSwim`, `Flight`, `GuiMove`, `NoSlowDown`, `Sprint`, `Velocity`

**Player (4)**
- `AntiVoid`, `NoBreakAnimation`, `Speedmine`, `XCarry`

**Render (12)**
- `AntiFog`, `BreakESP`, `Chams`, `ExeterGui`, `FullBright`, `HoleESP`, `ItemESP`, `NoHurtCam`, `ShulkerPreview`, `Skeleton`, `ToolTips`, `VoidESP`

**Misc (3)**
- `FakeVanilla`, `FovSlider`, `Time`

**HUD (8)**
- `HudEditor`
- Elementos: `PvPInfo`, `TextRadar`, `FeatureList`, `Position`, `Watermark`, `Potions`, `Totems`

### Mixins (24 en `xulu.mixins.json`)

`TitleScreenMixin`, `MixinConnection`, `MixinClientLevel`, `MixinEntity`, `MixinFontRenderer`, `MixinGui`,
`MixinLocalPlayer`, `MixinContainerPlayer`, `MixinC00Handshake`, `MixinMinecraft`,
`MixinMinecraftStop`, `MixinMultiPlayerGameMode`, `MixinLivingEntityRenderer`,
`MixinPlayerRenderer`, `MixinEndCrystalRenderer`, `MixinModelPlayer`, `MixinKeyboardInput`,
`MixinGameRenderer`, `MixinItemEntityRenderer`, `MixinScreenShulkerPreview`,
`AccessorServerboundMovePlayerPacket`, `AccessorLevelRenderer`, `AccessorClientTextTooltip`,
`AccessorMultiPlayerGameMode`.

### Eventos propios (18)

`CloseInventoryEvent`, `EntityEvent`, `EventAttackEntity`, `EventClickBlock`, `EventKey`,
`EventModelPlayerRender`, `EventModelRender`, `EventPlayerDamageBlock`, `EventPostRenderLayers`,
`EventReceivePacket`, `EventSendPacket`, `EventTotemPop`, `EventUpdate`, `LocalPlayerUpdateEvent`,
`MotionEvent`, `MotionEventPost`, `PlayerMoveEvent`, `RenderEvent`.

### Comandos (4)

`FriendCommand`, `EnemyCommand`, `FakePlayerCommand`, `AntiVoidCommand`.

---

## Cambios respecto al SRC original (1.12.2 → 1.18.2)

> El **changelog completo y actualizado de esta versión** está en el Discord oficial del port: **https://discord.gg/uUxbA4RAaS**.

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
- **AutoCrystal**:
  - `Fast Mode` (default OFF) es ahora un único toggle: al colocar un cristal envía en el mismo tick los ataques por paquete a los IDs de entidad predichos (`highestEntityId + 1..3`), sin esperar el spawn del servidor. Se eliminaron `Fast Type`, `Client Remove` y `Predict Range`; se agregó `City Bridge` (49 settings).
  - `City Bridge` (default ON, visible con `Place`): mientras AutoCity tiene un bloque del surround bloqueado, AutoCrystal deja de lado el loop normal de entidades y spamea cristales en/o alrededor de ese bloque. Prioridad: el bloque minado si ya es aire (surround roto), la posición exterior al mismo nivel (cara frontal exterior), arriba del bloque minado, arriba de la cara exterior y arriba del bloque de la cabeza. El cristal se elige con `canPlaceCrystal` y `Place Range`, y se filtra por daño propio (`Max Self Dmg`), daño al target (`Min Dmg`, salvo que sea letal) y distancia al target (13 bloques). El raytrace y el `Place` normal se omiten para el bridge; la rotación apunta al cristal, no a la base. Usa `AutoCity.getMiningBlock()` cuando el daño supera el 75% y cae a `getCityPos()` antes de eso. El bridge **no** se pausa por `Surround` expuesto/`AutoTrap`/`HoleFill`/`HoleBlocker`: es una colocación dirigida sobre la city y debe seguir funcionando en el momento del break.
  - **Fix `canPlaceCrystal`**: en 1.18.2 el aire de cuevas/void es `CAVE_AIR`/`VOID_AIR`, no `Blocks.AIR`; ahora se usa `BlockState#isAir()` y se validan los bounds del mundo para los dos espacios superiores. Esto también habilita el bridge dentro de cuevas.
  - **Fix `Auto Switch`/`Anti Weakness`**: en 1.18.2 cambiar solo `inventory.selected` no avisa al servidor; el `ServerboundUseItemOnPacket`/ataque seguía usando el ítem viejo y el cristal no se colocaba (o el golpe no aplicaba la espada). Ahora `setSlot` envía `ServerboundSetCarriedItemPacket` y sincroniza el slot local, y restaura el slot anterior al terminar. Esto también hace que `City Bridge` funcione con cristales en la hotbar, no solo en offhand.
  - Fix: el recorrido de `retryMap` usa un `Iterator`, evitando el `ConcurrentModificationException` que crasheaba el cliente.
  - Fix: los bounds de bloques se obtienen con `BlockInteractionHelper.getBlockBounds`, que soporta shapes vacíos.
- **ToolTips**:
  - El fondo y el texto custom se dibujan con depth test deshabilitado (como el original 1.12.2); antes el texto quedaba tapado por el rectángulo de fondo.
- **HoleESP**:
  - Port completo con los 20 settings del original (Frustum, HideOwn, Offset Lower, Future Mode, RenderDistance, Maximum Holes/Num, Hole Mode, RenderMode, DrawMode, Cuboid Height, Rainbow, colores Obi/Brock, Alpha y Outline Alpha) y los modos `SOLID`/`OUTLINE`/`FULL`/`CUBOID`/`INDICATOR`.
  - Detección de holes de bedrock/obsidiana, frustum culling, hideOwn y render con `XuluTessellator`; se agregaron `drawFullBox2`, `drawIndicator` y `drawBoundingBoxFace`.
- **Speedmine**:
  - `Render`/`Box`/`Outline`/`BoxAlpha`/`LineWidth` estaban muertos en el original (no había `onWorldRender`). En el port dibujan la caja y el outline del bloque minado con `XuluTessellator` en **todos** los modos (antes solo se trackeaba `currentPos` en `PACKET`).
  - `NoTrace`/`Pickaxe` también eran settings fantasma. Ahora `NoTrace` exige herramienta para el packet-mining y `Pickaxe` decide si debe ser un pico (`PickaxeItem`) o cualquier `DiggerItem`.
  - `Reset`, `AllowMultiTask` y `NoDelay` usan `AccessorMultiPlayerGameMode` (`destroyProgress`, `isDestroying`, `destroyDelay`).
  - `WebSwitch` cambia a la espada con `ServerboundSetCarriedItemPacket` (sin depender de `MiddleClickPearl`, aún no portado).
  - Nuevos eventos `EventClickBlock`/`EventPlayerDamageBlock` inyectados en `MultiPlayerGameMode.startDestroyBlock`/`continueDestroyBlock`.
  - **Fix `PACKET`**: el original enviaba `START`+`STOP` en cada tick. En 1.18.2 cada `START` resetea `destroyProgressStart` en el servidor y el `STOP` nunca alcanza el umbral de 0.7, por lo que el bloque no se rompía. Ahora se envía `START` una sola vez, se calcula el progreso local con `BlockState#getDestroyProgress` y se envía `STOP` repetidamente desde el 75% (margen sobre el 0.7 del servidor) hasta que el bloque desaparece. Sin predicción cliente, así los `BlockBreakAck` del servidor no revierten nada.
  - **Fix `DoubleBreak`**: el original rompía el bloque de arriba en el mismo tick, intercalando paquetes con el bloque principal y con `setBlockAndUpdate` cliente (que el servidor revertía). Ahora se encola el bloque de arriba (solo si es sólido/rompible y está dentro del alcance) y se rompe **después** de que el bloque principal sea aire, con la misma máquina de estados. El servidor 1.18.2 solo admite un `delayedDestroy` a la vez, por lo que la rotura es secuencial (documentado como limitación de la versión, no como bug).
  - **Fix `NoBreakAnim`**: se eliminó el spam de `ABORT` (rompía el packet-mining). Ahora `MixinClientLevel` cancela `ClientLevel#destroyBlockProgress` para el jugador local, ocultando el overlay de rotura en el cliente.
  - **Fix `INSTANT`**: se quitó el doble `setBlockAndUpdate`; la predicción cliente la hace `MultiPlayerGameMode#destroyBlock`.
- **AutoCity** (módulo nuevo, no existe en Xulu 1.5.2):
  - Rompe los bloques que rodean al target (surround) para exponerlo. Target no-friend más cercano con `OnlyHole` (bloque bajo sólido y al menos 2 lados sólidos), `TargetRange` y `Range` separados.
  - Settings (17): `Range`, `TargetRange`, `Delay`, `Mode` (`Packet`/`Legit`), `Rotate`, `Walls`, `OnlyHole`, `Feet`, `Head`, `InstantRebreak`, `Switch`, `Swing`, `Render`, `Box`, `Outline`, `BoxAlpha`, `LineWidth`.
  - `Feet`/`Head`: 4 cardinales a la altura de los pies y/o de la cabeza del target.
  - `Walls`: exige línea de visión al bloque con `ClipContext.Block.COLLIDER`.
  - `Switch`: cambia en silencio al mejor pico de la hotbar (`ServerboundSetCarriedItemPacket`, slot local y servidor) justo alrededor de cada `START`/`STOP` y restaura el slot inmediatamente. Así no pelea con el switch a cristal de AutoCrystal: cuando AutoCrystal mira el slot en el mismo tick ya está restaurado.
  - `Rotate`: rotación silenciosa con `BlockInteractionHelper.faceVectorPacketInstant`.
  - `Mode = Packet` envía `START` una sola vez y acumula el daño en el cliente tick a tick (progreso real del pico, igual que el `MineData` de AutoMine); al llegar al 75% manda `STOP+ABORT` con el pico equipado. `Legit` usa el mismo sistema pero espera al 100%. Ya no se manda `STOP` inmediato tras el `START` (en 1.18.2 eso solo armaba el delayed destroy y dejaba `isDestroyingBlock=false`). En 1.18.2 el servidor rompe al 70% (`ServerPlayerGameMode`), con timeout calculado con el progreso real (mínimo 100 ticks, máximo 400).
  - `InstantRebreak`: al romper, AutoCity **no** cambia de bloque: conserva `cityPos` y el `destroyProgressStart` del servidor. Mientras el bloque está en aire espera sin soltar el lock ni el target (aunque el target salte o se mueva dentro de `TargetRange`). Cuando el target lo re-coloca, manda `STOP+ABORT` en el mismo tick desde `ClientboundBlockUpdatePacket` **y** desde `ClientboundSectionBlocksUpdatePacket` (1.18.2 batchea cambios de una sección en combate). El `STOP` solo es instantáneo si el último `START` del cliente sigue siendo el de `cityPos`: AutoCity trackea los `START_DESTROY_BLOCK` salientes (`EventSendPacket`); si otro módulo o el minado vanilla lo robó, vuelve a minar desde cero (`START` + daño cliente) en vez de mandar un `STOP` que el servidor ignora. En creativo usa `START+STOP` (el servidor destruye en el `START`). Si el servidor no rompe en 10 ticks, vuelve al flujo normal de `startMine`. Con OFF, tras romper guarda `lastBrokenPos`, limpia el target de ciudad y elige otro bloque tras `Delay`.
  - API para el bridge/AutoCrystal: `getCityPos()`, `getMiningBlock()` (bloque con daño > 75%), `getBlockDamage()`, `isMiningActive()` e `isInstantMineComplete()`.
  - Bridge: expone `getCityPos()`/`getTargetPlayer()` para que AutoCrystal coloque cristales sobre el bloque bloqueado (ver **AutoCrystal → City Bridge**).
  - Render de caja/outline con `XuluTessellator`; enganchado en `ModuleManager` (Combat).
- **Aura**:
  - Rotación silenciosa funcional: se envía un `ServerboundMovePlayerPacket.Rot` con los ángulos calculados antes del ataque; los `Pos`/`StatusOnly` salientes se convierten a `PosRot`/`Rot` y los `Rot`/`PosRot` se corrigen vía `AccessorServerboundMovePlayerPacket` (yRot/xRot). Al perder el objetivo se envía un `Rot` con la rotación real para restaurar el servidor.
  - `LagCompensator` implementado (tick rate desde `ClientboundSetTimePacket`); antes era un stub que siempre devolvía 50 ms. Si no hay muestras devuelve 20 TPS.
  - `32k Switch` usa `EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack) >= 16` (NBT de 1.18.2) en lugar de leer el id numérico 16 de 1.12.2.
  - Fix del ray trace de pies: el original usaba `player.posX` como coordenada Y (`new Vec3d(posX, posX + eyeHeight, posZ)`); ahora usa `player.getY() + eyeHeight` y `ClipContext` con `Block.COLLIDER`/`Fluid.NONE`.
  - Acepta `ENCHANTED_GOLDEN_APPLE` además de `GOLDEN_APPLE` para el chequeo de gapple en offhand.
  - `EntityUtil.isMobAggressive`/`isNeutralMob`/`isHostileMob` portados (`ZombifiedPiglin`, `Wolf`, `EnderMan` y `MobCategory.MONSTER`).
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
  - Los elementos de texto (`FeatureList`, `TextRadar`, `PvPInfo`, `Position`, `Watermark`, `Potions`) alinean su contenido según el cuadrante en el que esté el frame: en la mitad derecha el texto se pega al borde derecho del frame y en la mitad inferior las líneas crecen hacia arriba desde el borde inferior. `Potions` expone `Align`/`Order` en modo `Auto` (default) y conserva los valores manuales `Left`/`Right` y `Up`/`Down`.
- **FeatureList**: nuevo setting `Auto Align` (default ON). Decide alineación derecha/izquierda y crecimiento hacia arriba/abajo según la posición del frame; si está OFF mandan `Aligned` y `Ordering`.
- **Watermark / Potions / Totems**:
  - `Watermark` recalcula tamaño con la fuente custom, alinea el texto al cuadrante y soporta `Rainbow`.
  - `Potions` usa `MobEffect#getDisplayName()` para el nombre y llama a `super.onEnable()`.
  - `Totems` cuenta también el totem de la offhand.

### Persistencia

- Se mantiene `Xulu/HUD.txt` para posiciones de HUD (y `Friends.txt` / `Enemies.txt`).
- **Limitación conocida:** todavía no existe guardado/carga de settings (`Value`) ni del estado toggled de los módulos; al reiniciar se pierden. Es uno de los pendientes para salir de alpha.

### No portado todavía

- Combat: 17 módulos (AutoLog, AutoWeb, MiddleClickPearl, SelfWeb, etc.).
- Movement: 8 (Step, Strafe, LongJump, PacketFly, Scaffold, etc.).
- Player: 10 (TpsSync, FastFall, AutoWalk, Freecam, etc.).
- Render: 35 (ESP suite, Xray, NoRender, Nametags, etc.).
- Misc: 25 (Timer, AntiSpam, CameraClip, etc.).
- HUD: 18 elementos (Crystals, Gapples, Obsidian, Exp, Armor, Target, etc.).

---

## Contenido del proyecto

**Repositorio oficial:** [Desaparecido2009/Xulu-Client](https://github.com/Desaparecido2009/Xulu-Client). El port se publica únicamente en ese repositorio; nunca se sube a `Elementars/Xulu-v1.5.2`.

Archivos que forman parte del proyecto y se necesitan para compilar:

```
.gitignore
LICENSE
README.md
build.gradle
settings.gradle
gradle.properties
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
src/
```

**No formar parte del proyecto compilable (solo referencia o artefactos locales):**

```
xuluv1.5.2/     # código original decompilado (referencia, no redistribuir)
build/          # artefactos de compilación
.gradle/        # caché de Gradle
run/            # entorno de ejecución local
.idea/ .settings/ bin/ *.iml .classpath .project .factorypath
Xulu/           # configuración generada en runtime
```

El único destino de push es el repositorio propio del dueño (`Desaparecido2009/Xulu-Client`). Nunca se hace push al repositorio original de Xulu (`Elementars/Xulu-v1.5.2`) ni se sube `xuluv1.5.2/`.

---

## Licencia

Este port se distribuye bajo **GNU General Public License v3.0 only** (`GPL-3.0-only`). Ver [LICENSE](LICENSE).

Aviso importante: el proyecto original **Xulu v1.5.2** es propiedad de sus autores y su código decompilado figura como *All Rights Reserved*. Este proyecto no incluye ese código original. Publicar y redistribuir un port derivado requiere contar con permiso del titular de los derechos del original; la GPLv3 aplica al código de este port en la medida en que quien lo publique tenga derechos para licenciarlo así.

## Créditos

- **Elementars** y autores de Xulu por el cliente original.
- **Xdolf** por el font renderer.
- Comunidad de XuluPort por el trabajo de porteo a 1.18.2.
