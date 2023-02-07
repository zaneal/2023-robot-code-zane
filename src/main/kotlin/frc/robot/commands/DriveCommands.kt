package frc.robot.commands


import com.pathplanner.lib.PathPlannerTrajectory
import com.pathplanner.lib.commands.PPSwerveControllerCommand
import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.controller.ProfiledPIDController
import edu.wpi.first.math.trajectory.Trajectory
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj2.command.*
import frc.robot.constants.DrivetrainConstants
import frc.robot.constants.TrajectoryConstants
import frc.robot.subsystems.SwerveSubsystem
class UnlimitedDrive(val swerveSubsystem: SwerveSubsystem, val forward: () -> Double, val sideways: () -> Double, val radians: () -> Double, val fieldRelative: Boolean) : CommandBase() {

    init {
        addRequirements(swerveSubsystem)
    }

    override fun execute() {
        val forwardDesired = MathUtil.applyDeadband(forward(), 0.06)
        val sidewaysDesired = MathUtil.applyDeadband(sideways(), 0.06)
        val radiansDesired = MathUtil.applyDeadband(radians(), 0.06)

        swerveSubsystem.drive(forwardDesired, sidewaysDesired, -1 * radiansDesired, fieldRelative, true)
    }

    override fun end(interrupted: Boolean) {
        swerveSubsystem.drive(0.0,0.0,0.0,true, false)
    }
}

fun TrajectoryDrive(swerveSubsystem: SwerveSubsystem, trajectory: Trajectory) : Command {
    var thetaController = ProfiledPIDController(
        TrajectoryConstants.kPThetaController, 0.0, TrajectoryConstants.kDThetaController, TrajectoryConstants.kThetaControllerConstraints
    )
    thetaController.enableContinuousInput(-Math.PI, Math.PI)

    var thetaControllerError = NetworkTableInstance.getDefault().getTable("Swerve").getDoubleTopic("TurningError").getEntry(0.0)

    thetaControllerError.set(thetaController.positionError)

    var swerveControllerCommand = SwerveControllerCommand(
        trajectory,
        swerveSubsystem::pose,
        DrivetrainConstants.driveKinematics,

        // Position controllers
        PIDController(TrajectoryConstants.kPXController, 0.0, 0.0),
        PIDController(TrajectoryConstants.kPYController, 0.0, 0.0),
        thetaController,
        swerveSubsystem::setModuleStates,
        swerveSubsystem
    )

    // Reset odometry to the starting pose of the trajectory.

    println("trajectoryRunning")

    // Run path following command, then stop at the end.
    return ParallelCommandGroup(
        SequentialCommandGroup(
            swerveControllerCommand,
            RunCommand({
                swerveSubsystem.drive(0.0,0.0,0.0,false, false)
            })
        ),

        RunCommand({
            println("-------------------------")
//                println(thetaController.velocityError)
//                println(thetaController.positionError)
//                println(thetaController.setpoint.position)
//                println(thetaController.setpoint.velocity)
//                println(swerveSubsystem.heading)
            println(swerveSubsystem.pose.x)
            println(swerveSubsystem.pose.y)
            thetaControllerError.set(thetaController.positionError)

            println("-------------------------")
        })
    )
}


fun TrajectoryDrivePathPlanner(swerveSubsystem: SwerveSubsystem, trajectory: PathPlannerTrajectory, isFirstPath: Boolean) : Command {
    var thetaController = PIDController(
        TrajectoryConstants.kPThetaController, 0.0, TrajectoryConstants.kDThetaController
    )
    thetaController.enableContinuousInput(-Math.PI, Math.PI)

    var thetaControllerError = NetworkTableInstance.getDefault().getTable("Swerve").getDoubleTopic("TurningError").getEntry(0.0)

    thetaControllerError.set(thetaController.positionError)

    var swerveControllerCommand = PPSwerveControllerCommand(
        trajectory,
        swerveSubsystem::pose,
        DrivetrainConstants.driveKinematics,

        // Position controllers
        PIDController(TrajectoryConstants.kPXController, 0.0, 0.0),
        PIDController(TrajectoryConstants.kPYController, 0.0, 0.0),
        thetaController,
        swerveSubsystem::setModuleStates,
        true,
        swerveSubsystem
    )

    // Reset odometry to the starting pose of the trajectory.

    // Run path following command, then stop at the end.
    return SequentialCommandGroup(
        InstantCommand({
            // Reset odometry for the first path you run during auto
            if(isFirstPath){
                swerveSubsystem.resetOdometry(trajectory.getInitialHolonomicPose());
            }
        }),
        swerveControllerCommand,
        RunCommand({
            swerveSubsystem.drive(0.0,0.0,0.0,false, false)
        })
    )
}
