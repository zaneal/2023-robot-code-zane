package frc.robot.constants

import edu.wpi.first.math.trajectory.TrapezoidProfile


object ArmConstants {
    /** make a network table value in table Arm and value ElbowPosition**/
    val elbowMotorId = 9
    val elevatorMotorId = 3

    val topLinebreakerId = 0
    val bottomLinebreakerId = 1

    val elevatorMotorInverted = true
    val intakeMotorInverted = false

    //PID vals
    //PID stuff
    //also needs tuning
    val elevatorP = 5.0 //500
    val elevatorI = 0.0
    val elevatorD = 0.0
    val elevatorTrapezoidConstraints = TrapezoidProfile.Constraints(10.0, 0.8)

    val elbowP = 0.1
    val elbowI = 0.1
    val elbowD = 0.1

    val elevatorMinHeight = 0.0
    val elevatorMaxHeight = 0.9245

    val elbowMaxRotation = (3.0/4.0) * Math.PI
    val elbowMinRotation = -(1.0/4.0) * Math.PI

    //multipliers for unit conversion and stuff
    // these values obtained from tuning
    val elevatorEncoderVelocityMultiplier = 0.003010870139 / 60.0 //this should turn revs/min to meters/sec
    val elevatorEncoderPositionConversionFactor = 0.003010870139 //this should turn revs to meters

    val elbowEncoderVelocityMultiplier = 2.0 //this should turn revs/min to radians/sec (Yay!)
    val elbowEncoderPosMultiplier = 2.0 //this should turn revs to radians (Yay again!)
    val elbowEncoderPosOffset = 0.0

    //FF vals
    val elbowFeedforwardKA = 0.0
    val elbowFeedforwardKG = 0.0
    val elbowFeedforwardKS = 0.0
    val elbowFeedforwardKV = 0.0

    //all those random command values
    //knowing the mechanism, these will end up being really strange values for all the way down.
//bottom
    val elevatorBottomPos = 0.0
    val elbowBottomPos = 0.0

    //top peg
    val elevatorTopPegPos = 3.0
    val elbowTopPegPos = 3.0

    //middle peg
    val elevatorMidPegPos = 2.0
    val elbowMidPegPos = 2.0

    //ground
    val elevatorGroundPos = 2.0
    val elbowGroundPos = 1.0

    //middle shelf
    val elevatorMidShelfPos = 2.0
    val elbowMidShelfPos = 2.0

    //top shelf
    val elevatorTopShelfPos = 3.0
    val elbowTopShelfPos = 3.0

    //pickup shelf position
    val elevatorPickupPos = 3.0
    val elbowPickupPos = 3.0




}