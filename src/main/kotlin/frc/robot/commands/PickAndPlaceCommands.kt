package frc.robot.commands

import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.controller.ProfiledPIDController
import edu.wpi.first.math.trajectory.TrapezoidProfile
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandBase
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import frc.robot.constants.ArmConstants
import frc.robot.subsystems.PickAndPlaceSubsystem

class SetPickAndPlacePosition(val continuous: Boolean ,val subsystem: PickAndPlaceSubsystem,
                          val elevatorSupplier: ()->Double,
                          val elbowSupplier: ()->Double,
                          val wristSupplier:()->Double,
                          val intakeSupplier: ()->Double,
                          ) : CommandBase() {
    var first = true
    var elevatorPid = ProfiledPIDController(
        ArmConstants.elevatorP,
        ArmConstants.elevatorI,
        ArmConstants.elevatorD,
        TrapezoidProfile.Constraints(2.0, 0.5)//3.0, 0.8
    )
    var elbowPid = ProfiledPIDController(
        ArmConstants.elbowP,
        ArmConstants.elbowI,
        ArmConstants.elbowD,
        TrapezoidProfile.Constraints(Math.PI/16, Math.PI/16)//I think might need to be slowed down, cause robot can drive while the elbow is moving as long as elevator is down
    )

    var wristPid = ProfiledPIDController(
        ArmConstants.wristP,
        ArmConstants.wristI,
        ArmConstants.wristD,
        TrapezoidProfile.Constraints(Math.PI/4, Math.PI/16)
    )
    val elbowFeedforward = ArmConstants.elbowFF

    val wristFeedforward = ArmConstants.wristFF
    init {
        addRequirements(subsystem)
        first = true

        elevatorPid.setTolerance(0.03)//might need changing
        elbowPid.setTolerance(0.05)//might need changing
        wristPid.setTolerance(0.04)//might need changing
    }//This might need to be above the variables

    object Telemetry{
        val desiredElbow = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("DesiredElbow").publish()
        val desiredWrist = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("DesiredWrist").publish()
        val desiredElevator = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("DesiredElevator").publish()
        val desiredIntake = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("DesiredIntake").publish()

        val elbowPidPosError = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("ElbowPidError").publish()
        val elevatorPidPosError = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("ElevatorPidError").publish()
        val wristPidPosError = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("WristPidError").publish()

        val elbowPidVelocityError = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("ElbowPidError").publish()
        val elevatorPidVelocityError = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("ElevatorPidError").publish()
        val wristPidVelocityError = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("WristPidError").publish()

        val elbowGoal = NetworkTableInstance.getDefault().getTable("Arm").getDoubleTopic("Elbow Goal").publish()
    }

    override fun execute() {

        // store a lastValue for each of these, compare, and then set/reset if they are changed
        if (first){
            elevatorPid.reset(subsystem.elevatorPositionMeters)
            elbowPid.reset(subsystem.elbowPositionRadians)
            wristPid.reset(subsystem.absoluteWristPosition)

            elevatorPid.setGoal(subsystem.elevatorPositionMeters)
            elbowPid.setGoal(subsystem.elbowPositionRadians)
            wristPid.setGoal(subsystem.absoluteWristPosition)
//these could be causing the slow at first run.
            subsystem.elevatorVoltage = elevatorPid.calculate(subsystem.elevatorPositionMeters,elevatorSupplier().coerceIn(ArmConstants.elevatorMinHeight, ArmConstants.elevatorMaxHeight))
            subsystem.elbowVoltage = elbowPid.calculate(subsystem.elbowPositionRadians,elbowSupplier().coerceIn(ArmConstants.elbowMinRotation, ArmConstants.elbowMaxRotation)) + elbowFeedforward.calculate(subsystem.elbowPositionRadians, 0.0)
            subsystem.wristVoltage = wristPid.calculate(subsystem.absoluteWristPosition,wristSupplier().coerceIn(ArmConstants.wristMinRotation, ArmConstants.wristMaxRotation)) + wristFeedforward.calculate(subsystem.absoluteWristPosition, 0.0)
            subsystem.intakesVoltage = intakeSupplier()

//            subsystem.elevatorVoltage = elevatorPid.calculate(subsystem.elevatorPositionMeters)
//            subsystem.elbowVoltage = elbowPid.calculate(subsystem.elbowPositionRadians) + elbowFeedforward.calculate(subsystem.elbowPositionRadians, 0.0)
//            subsystem.wristVoltage = wristPid.calculate(subsystem.absoluteWristPosition) + wristFeedforward.calculate(subsystem.absoluteWristPosition, 0.0)
//            subsystem.intakesVoltage = intakeSupplier()

        } else {
            subsystem.elevatorVoltage = elevatorPid.calculate(subsystem.elevatorPositionMeters)

            subsystem.elbowVoltage = elbowPid.calculate(subsystem.elbowPositionRadians) + elbowFeedforward.calculate(subsystem.elbowPositionRadians, 0.0)
            subsystem.wristVoltage = wristPid.calculate(subsystem.absoluteWristPosition) + wristFeedforward.calculate(subsystem.absoluteWristPosition, 0.0)

            subsystem.intakesVoltage = intakeSupplier()
        }


        // TODO: Add telemetry: desired states (from suppliers), pid errors, voltages
        Telemetry.desiredWrist.set(wristSupplier())
        Telemetry.desiredElbow.set(elbowSupplier())
        Telemetry.desiredElevator.set(elevatorSupplier())
        Telemetry.desiredIntake.set(intakeSupplier())
        Telemetry.elbowPidPosError.set(elbowPid.positionError)
        Telemetry.elevatorPidPosError.set(elevatorPid.positionError)
        Telemetry.wristPidPosError.set(wristPid.positionError)
        Telemetry.elbowPidVelocityError.set(elbowPid.velocityError)
        Telemetry.elevatorPidVelocityError.set(elevatorPid.velocityError)
        Telemetry.wristPidVelocityError.set(wristPid.velocityError)

        Telemetry.elbowGoal.set(elbowPid.goal.position)

        first = false
    }

    override fun isFinished(): Boolean {
        return !first && !continuous && (elbowPid.atGoal() && elevatorPid.atGoal() && wristPid.atGoal())
    }

    //TODO: Could be messing with it:
    override fun end(interrupted: Boolean) {
        // TODO: does zeroing the intake voltage cause the piece to drop?
        subsystem.elevatorVoltage = 0.0
        subsystem.wristVoltage = 0.0
        subsystem.elbowVoltage = 0.0
        subsystem.intakesVoltage = 0.0
    }
}

class zeroVoltage(val subsystem: PickAndPlaceSubsystem): CommandBase() {

    init {
        addRequirements(subsystem)
    }

    override fun execute() {
        subsystem.wristVoltage = 0.0
        subsystem.elevatorVoltage = 0.0
        subsystem.elbowVoltage = 0.0
        subsystem.intakesVoltage = 0.0
    }



}
//TODO: All values for functions must be tested with degrees, height, and vision on a field
fun Base(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.0 }, // elevator
            { Math.toRadians(87.0) }, // elbow
            { Math.toRadians(20.0) }, // wrist
            { 2.0 } // intake
        )

    )
}
fun MidPlace(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(65.0) }, // elbow
            { Math.toRadians(20.0) }, // wrist
            { 1.5 } // intake
        ),

        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(25.0) }, // elbow
            { 0.0 }, // wrist
            { 1.5 } // intake
        ),

        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(25.0) }, // elbow
            { 0.0 }, // wrist
            { -2.0 } // intake
        )
    )
}
fun HighPlace(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(65.0) }, // elbow
            { Math.toRadians(20.0) }, // wrist
            { 1.5 }
        ),

        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(20.0) }, // elbow
            { 0.0 }, // wrist
            { 1.5 }
        ),

        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(20.0) }, // elbow
            { 0.0 }, // wrist
            { -2.0 }
        )
    )
}
fun MidPlaceSide(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(65.0) }, // elbow
            { Math.toRadians(20.0) }, // wrist
            { 1.5 } // intake
        ),

        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(25.0) }, // elbow
            { 0.0 }, // wrist
            { 1.5 } // intake
        ),

        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(25.0) }, // elbow
            { 0.0 }, // wrist
            { -2.0 } // intake
        )
    )
}
fun HighPlaceSide(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(65.0) }, // elbow
            { Math.toRadians(20.0) }, // wrist
            { 1.5 }
        ),

        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(20.0) }, // elbow
            { 0.0 }, // wrist
            { 1.5 }
        ),

        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(20.0) }, // elbow
            { 0.0 }, // wrist
            { -2.0 }
        )
    )
}
fun LowPickCone(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.1 }, // elevator
            { Math.toRadians(-10.0) }, // elbow
            { 0.0 }, // wrist
            { 0.0 } // intake
        ),
        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.0 }, // elevator
            { Math.toRadians(-10.0) }, // elbow
            { 0.0 }, // wrist
            { 2.0 } // intake
        )
    )
}
fun LowPickCube(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.4 }, // elevator
            { Math.toRadians(0.0) }, // elbow
            { 0.0 }, // wrist
            { 0.0 } // intake
        ),

        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.4 }, // elevator
            { Math.toRadians(-27.0) }, // elbow
            { 0.0 }, // wrist
            { 0.0 } // intake
        ),

        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(-27.0) }, // elbow
            { 0.0 }, // wrist
            { 4.0 } // intake
        )
    )
}
fun AutoPick(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.4 }, // elevator
            { Math.toRadians(-27.0) }, // elbow
            { 0.0 }, // wrist
            { 0.0 } // intake
        ),

        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(-27.0) }, // elbow
            { 0.0 }, // wrist
            { 4.0 } // intake
        )
    )
}
fun AutoPlaceMid(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(65.0) }, // elbow
            { Math.toRadians(20.0) }, // wrist
            { 1.5 } // intake
        ),

        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(25.0) }, // elbow
            { 0.0 }, // wrist
            { 1.5 } // intake
        ),

        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.2 }, // elevator
            { Math.toRadians(25.0) }, // elbow
            { 0.0 }, // wrist
            { -2.0 } // intake
        )
    )
}
fun AutoPlaceHigh(pnp: PickAndPlaceSubsystem): Command {
    return SequentialCommandGroup(
        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(65.0) }, // elbow
            { Math.toRadians(20.0) }, // wrist
            { 1.5 }
        ),

        SetPickAndPlacePosition(
            false,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(20.0) }, // elbow
            { 0.0 }, // wrist
            { 1.5 }
        ),

        SetPickAndPlacePosition(
            true,
            pnp,
            { 0.7 }, // elevator
            { Math.toRadians(20.0) }, // elbow
            { 0.0 }, // wrist
            { -2.0 }
        )
    )
}

class VoltageArm(val subsystem: PickAndPlaceSubsystem,
                 val elevatorSupplier: ()->Double,
                 val elbowSupplier: ()->Double,
                 val wristSupplier:()->Double,
                 val intakeSupplier: ()->Double) : CommandBase() {
    init {
        addRequirements(subsystem)
    }

    override fun execute() {
        subsystem.elevatorVoltage = elevatorSupplier()
        subsystem.elbowVoltage = elbowSupplier()
        subsystem.wristVoltage = wristSupplier()
        subsystem.intakesVoltage = intakeSupplier()
    }

    override fun end(interrupted: Boolean) {

        subsystem.elevatorVoltage = 0.0
        subsystem.wristVoltage = 0.0
        subsystem.elbowVoltage = 0.0
        subsystem.intakesVoltage = 0.0
    }


}
