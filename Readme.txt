1. How to run:

Double click SuperMarioBoulderDash.jar it will run contest10.map with AI.

Or use command to specify map and the way to play:

Java -jar SuperMarioBoulderDash.jar contest10.map ExhaustiveMiner

The map name "contest10.map" can be replaced by following 20 maps (case sensitive):

contest1.map
contest2.map
...
contest10.map
lightning1.map
lightning2.map
...
lightning10.map

The play mode "ExhaustiveMiner" can be replaced by following 4 ways (case sensitive):

ArrowMiner (User can use arrow key on the keyboard to control the Super Mario.)
ExhaustiveMiner (The artificial intelligence to play game by brute force 5 steps further.)
RandomWalk (Auto play by random moves.)
DefaultMiner (Play by type in instruction character to console: A (abort), U (up), D (down), L (left), R (right), and W (wait).)

2. How to compile:

CD src
Javac *.java
Java Lift

Make sure "Maps" and "resources" folder are in the same path with class files.

3. Reference

Problem idea comes from:
ICFP Programming Contest 2012 Lambda Lifting
http://www-fp.cs.st-andrews.ac.uk/~icfppc/task.pdf

The rule changed a little for rationality:
Dillon's rule: Miner can keep going down as the same speed with the falling rock above his head.

Program task comes from:
http://cs.fit.edu/~ryan/cse4051/projects/lift/

Sounds and images comes from: Internet
