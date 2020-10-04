Hyper Loop Runner
=================

Game developed for [Ludum Dare 47](https://ldjam.com/events/ludum-dare/47) 48h Compo (Theme: Stuck in a Loop)

[Game page](https://ldjam.com/events/ludum-dare/47/hyper-loop-runner)

*Warning:*

As expected from a 48h competition entry, the quality of this code might hurt your eyes.

# Story

The time rift’s containment device has been breached! To fix it, you need to travel through time and stop the breach.

To travel through time, you need to hit the rift’s blue side.

**WATCH OUT**, though! During time travel, the rift will follow you in a loop, don’t let the red side hit you, or you’ll disappear from existence.

Run laps until you can hit the blue side and perform a time jump!

# How to Play

- **Arrow keys**: Control your ship
- **Space**: Boost

**Goal**: Run laps around the track until you hit the time rift’s blue side. Don’t let the red side hit you!

# Dev notes

Hi there, I hope that you enjoyed my game :).

The main concept was based around sonic mania special stages, where you have to run in circles around a track until you hit an UFO. I ended up changing the game play a bit though.

The graphics were based on old SNES Mode 7 games, even the game's internal resolution is 256x224, to mimic the SNES :).

The game was written in Scala using Minart (https://github.com/jd557/minart), which is a graphics library that I'm developing. In theory, it should be possible to compile the game to javascript and to a native binary (without sound support), but I hit some performance problems, so I couldn't do that.

The music was composed using TuxGuitar and the sprites were drawn in Gimp.
