# Maze Runner

A command-line Java app that generates, saves, loads, displays, and solves
mazes. Built as part of the JetBrains Academy "Maze Runner" project.

## Features

- **Generate** a random, perfectly-solvable maze of any size using randomized
  Kruskal's algorithm (minimum spanning tree over a grid of rooms).
- **Save** the current maze to a text file.
- **Load** a maze from a text file.
- **Display** the maze in the console.
- **Find the escape** — computes the shortest path from the entrance to the
  exit with a breadth-first search and marks it with `/` characters. The path
  is display-only: it is never written to a saved file, and it does not
  affect what `Display the maze` shows afterwards.

The menu only offers save/display/find-escape once a maze exists (generated
or loaded):

```
=== Menu ===
1. Generate a new maze
2. Load a maze
3. Save the maze
4. Display the maze
5. Find the escape
0. Exit
```

## Example

```
=== Menu ===
1. Generate a new maze
2. Load a maze
0. Exit
>2
>maze.txt

=== Menu ===
1. Generate a new maze
2. Load a maze
3. Save the maze
4. Display the maze
5. Find the escape
0. Exit
>5
██████████████████████████████████████████████████//██████████
██      ██                          ██  ██//////////██  ██  ██
██  ██████████████  ██████████████████  ██//██████████  ██  ██
...
```

## Project structure

```
Maze Runner (Java)/task/src/maze/
├── Main.java           # menu loop, I/O, BFS escape finder
└── MazeGenerator.java  # random maze generation (Kruskal's MST)
```

## Building and running

The sources have no external dependencies, so they can be compiled directly
with the JDK (Java 17+):

```bash
cd "Maze Runner (Java)/task/src"
javac -d out maze/Main.java maze/MazeGenerator.java
java -cp out maze.Main
```

The repository also includes the Gradle wrapper used by the JetBrains
Academy plugin (`./gradlew test`) for running the course's own test suite
under `Maze Runner (Java)/task/test`.

## How "Find the escape" works

The maze is treated as an unweighted grid graph: each open (non-wall) cell is
a node, and each pair of adjacent open cells is an edge of weight 1. A
breadth-first search from the entrance opening (top border) explores the
grid level by level, so the first time it reaches the exit opening (bottom
border) it has necessarily done so via a shortest path. The visited path is
reconstructed via parent pointers and marked with `/` on a throwaway copy of
the maze — the original grid used for saving and later display is never
mutated.
