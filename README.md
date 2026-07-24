# Diosesmon Tracker (mod de Fabric)

Mod de **solo cliente** que trackea:

1. **Tus Pokémon** — capturas, evoluciones, eclosiones de huevos.
2. **Progreso de eventos de Crianza y Caza** — comparando cada eclosión/captura
   contra una lista configurable de especies objetivo (vos la actualizás
   cuando cambia el evento).
3. **Precios del GTS** — lee los listados visibles mientras vos navegás el
   GTS manualmente, y guarda un historial para calcular precios promedio.

Todo se guarda en archivos JSON locales en:
```
.minecraft/config/diosesmon-tracker/
    pokemon_log.json
    event_targets.json   (lo editás vos)
    event_progress.json
    gts_listings.json
```

## ⚠️ Antes de nada: confirmá con el staff

Este mod se inyecta en tu cliente de Minecraft. Aunque **no automatiza
ninguna acción del juego** (no mueve el mouse, no clickea, no manda
paquetes falsos — solo lee datos que el juego ya te muestra), muchos
servidores solo permiten una lista fija de mods. Antes de instalarlo:

- Revisá las reglas de Diosesmon sobre mods de cliente.
- Si tenés dudas, preguntale directamente al staff si un mod que **solo
  lee y guarda información en tu compu** (sin modificar tu gameplay) está
  permitido. La parte de GTS en particular roza "recolección de datos del
  mercado", que algunos servidores regulan aparte.

## ⚠️ Esto es un scaffold, no un mod terminado

Yo (Claude) no tengo acceso a los repositorios de Minecraft/Fabric/Cobblemon
desde mi entorno, así que no pude compilar ni probar este código. Estructura
y lógica están completas, pero **tenés 3 cosas que ajustar** antes de que
funcione:

### 1. Versión y dependencia de Cobblemon (`build.gradle` / `gradle.properties`)

Reemplazá los valores de ejemplo por la versión real de Minecraft/Cobblemon
que usa Diosesmon, y la coordenada Maven correcta de Cobblemon (revisá la
guía oficial de setup de Cobblemon para desarrolladores — cambia seguido
entre versiones, por eso no puse una URL fija).

### 2. Nombres reales de eventos de Cobblemon (`PersonalPokemonTracker.java`)

Dejé el pseudocódigo comentado de cómo suscribirse a `CobblemonEvents`
(captura, evolución, eclosión). Los nombres exactos de esos eventos pueden
variar según la versión de Cobblemon instalada. Abrí el código fuente de
Cobblemon (es open source) y buscá la clase `CobblemonEvents` para
confirmar los nombres antes de descomentar esa parte.

### 3. Formato real de la pantalla de GTS (`GtsPriceTracker.java`)

No conozco el formato exacto que usa Diosesmon para mostrar precios en el
GTS. Abrí el GTS en el juego, fijate el texto exacto (nombre del ítem +
lore/descripción), y ajustá:
- `GTS_SCREEN_TITLE_HINT`: un fragmento del título de la pantalla.
- `PRICE_PATTERN`: la expresión regular que reconoce el precio.

## Cómo compilar

1. Instalá [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community alcanza).
2. Instalá el entorno de desarrollo de Fabric siguiendo la
   [guía oficial de Fabric](https://fabricmc.net/wiki/tutorial:setup) para
   la versión de Minecraft que corresponda.
3. Abrí esta carpeta como proyecto Gradle en IntelliJ.
4. Ajustá los 3 puntos de arriba.
5. Corré `./gradlew build` — el `.jar` te queda en `build/libs/`.
6. Copialo a la carpeta `mods/` de tu instalación de Fabric (junto con
   Cobblemon y Fabric API, que también necesitás tener instalados).

## Cómo usar una vez instalado

- **Pokémon:** se registra solo, no hace falta nada.
- **Eventos:** editá `event_targets.json` con las especies del evento
  actual de Crianza y Caza. El mod cuenta automáticamente cada captura/
  eclosión que matchee.
- **GTS:** abrí el GTS como siempre y quedate 2-3 segundos en cada página
  para que el mod la registre. Con el tiempo se va armando un historial de
  precios por ítem.

## Conectarlo con el diario web

Los archivos JSON que genera este mod tienen el mismo espíritu que el
diario web que armamos antes. Si querés, después le agrego al diario una
función de "importar" que lea estos archivos directo y te llene las
tarjetas (nivel, capturas, etc.) sin que tengas que tipear nada.
