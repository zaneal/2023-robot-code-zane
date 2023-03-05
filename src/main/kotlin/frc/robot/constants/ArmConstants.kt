package frc.robot.constants

import edu.wpi.first.math.controller.ArmFeedforward
import edu.wpi.first.math.trajectory.TrapezoidProfile


object ArmConstants {
    /** make a network table value in table Arm and value ElbowPosition**/
    val elbowMotorId = 10
    val elevatorMotorId = 9

    val topLinebreakerId = 0
    val bottomLinebreakerId = 1

    val elevatorMotorInverted = false
    val elbowMotorInverted = true
    val intakeMotorInverted = false

    //Need very small tune
    val elevatorP = 150.0 //500
    val elevatorI = 0.0
    val elevatorD = 0.0
    //val elevatorTrapezoidConstraints = TrapezoidProfile.Constraints(50.0, 30.0)
//tune:
    val elbowP = 5.0 //5
    val elbowI = 0.0
    val elbowD = 0.0
    //maybe change:
    val elbowFF = ArmFeedforward(0.13, 0.50, 1.00)//might need to change kg

    //tune:
    val wristP = 3.0
    val wristI = 0.0
    val wristD = 0.0
    //maybe change:
    val wristFF = ArmFeedforward(0.13, 1.50, 1.00)

    val elevatorMinHeight = 0.0
    val elevatorMaxHeight = 0.9245

    val elbowMaxRotation = (1.0/2.0) * Math.PI
    val elbowMinRotation = -(1.0/2.0) * Math.PI

    val wristMaxRotation = (Math.PI/2.0)
    val wristMinRotation = -(Math.PI/2.0)

    // multipliers for unit conversion and stuff
    // these values obtained from tuning
    val elevatorEncoderVelocityConversionFactor = (0.003010870139 * 2.4) / 60.0 //this should turn revs/min to meters/sec
    val elevatorEncoderPositionConversionFactor = 0.003010870139 * 2.4 //this should turn revs to meters

    //could need small tuning:
    val elbowEncoderPosOffset = -2.727
    val wristEncoderPosOffset = -0.0925

    val elevatorZeroingVoltage = -1.0
}