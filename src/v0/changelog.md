# Key Changes
- Explore copied over from bc23
    - water is equivalent to a wall, bridging not implemented
- Separated main phase and setup phase into different files
- Random/Constants copied over
    - `RobotController rc` is a static constant now, not passed through func params
- Uses the turtle strategy where each unit randomly selects a spawn point and stays around it and shoots/places traps