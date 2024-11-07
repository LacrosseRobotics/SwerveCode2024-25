// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.hal.SimDouble;
import edu.wpi.first.hal.simulation.SimDeviceDataJNI;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.ADIS16448_IMU.IMUAxis;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.OperatorConstants;
import frc.robot.subsystems.swervedrive.SwerveSubsystem;

import java.io.File;
import java.io.IOException;

import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;

import com.kauailabs.navx.frc.AHRS;

import swervelib.SwerveDrive;
import swervelib.parser.SwerveParser;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to each mode, as
 * described in the TimedRobot documentation. If you change the name of this class or the package after creating this
 * project, you must also update the build.gradle file in the project.
 */

public class Robot extends TimedRobot
{
  
  private static Robot   instance;
  private        Command m_autonomousCommand;

  private RobotContainer m_robotContainer;

  private Timer disabledTimer;
  private Timer timer;

  final CommandXboxController driverXbox = new CommandXboxController(0);

  private final AddressableLED m_led;
  private final AddressableLEDBuffer m_ledBuffer;

  double LedFlash = 30;
  int Speedled;

  public Robot()
  {
    instance = this;
    stick = new Joystick(0);
        try {
            /* Communicate w/navX MXP via the MXP SPI Bus.                                     */
            /* Alternatively:  I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB     */
            /* See http://navx-mxp.kauailabs.com/guidance/selecting-an-interface/ for details. */
            ahrs = new AHRS(SPI.Port.kMXP);
        } catch (RuntimeException ex ) {
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
        }

    // PWM port 9
    // Must be a PWM header, not MXP or DIO
    m_led = new AddressableLED(9);

    // Reuse buffer
    // Default to a length of 60, start empty output
    // Length is expensive to set, so only set it once, then just update data
    m_ledBuffer = new AddressableLEDBuffer(60);
    m_led.setLength(m_ledBuffer.getLength());

    // Set the data
    m_led.setData(m_ledBuffer);
    m_led.start();

  }

  public static Robot getInstance()
  {
    return instance;
  }

  /**
   * This function is run when the robot is first started up and should be used for any initialization code.
   */
  @Override
  public void robotInit()
  {
    timer = new Timer();
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();

    // Create a timer to disable motor brake a few seconds after disable.  This will let the robot stop
    // immediately when disabled, but then also let it be pushed more 
    disabledTimer = new Timer();

  
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics that you want ran
   * during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic()
  {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
  
  }

  /**
   * This function is called once each time the robot enters Disabled mode.
   */
  @Override
  public void disabledInit()
  {
    m_robotContainer.setMotorBrake(true);
    disabledTimer.reset();
    disabledTimer.start();
    
  }

  @Override
  public void disabledPeriodic()
  {
    if (disabledTimer.hasElapsed(Constants.DrivebaseConstants.WHEEL_LOCK_TIME))
    {
      m_robotContainer.setMotorBrake(true);
      disabledTimer.stop();
    }
    
    SmartDashboard.putString(   "LedColor",        "Yellow");

    //LED ON
    for (var i = 0; i < LedFlash; i++) {
      // Sets the specified LED to the RGB values for Yellow
      m_ledBuffer.setRGB(i, 255, 165, 0);
   }
   
   m_led.setData(m_ledBuffer);

  }

  /**
   * This autonomous runs the autonomous command selected by your {@link RobotContainer} class.
   */
  @Override
  public void autonomousInit()
  {
    m_robotContainer.setMotorBrake(true);
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null)
    {
      m_autonomousCommand.schedule();
    }
    timer.reset();
    timer.start();
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic()
  {
  
    SmartDashboard.putString(   "LedColor",        "Purple");

    //LED Blink
      if (timer.get() < 0.25){
        for (var i = 0; i < LedFlash; i++) {
          // Sets the specified LED to the RGB values for OFF
          m_ledBuffer.setRGB(i, 0, 0, 0);
       }
       
       m_led.setData(m_ledBuffer);
      }
      else if (timer.get() < .5){
        for (var i = 0; i <LedFlash; i++) {
          // Sets the specified LED to the RGB values for Purple
          m_ledBuffer.setRGB(i, 255, 0, 255);
       }
       
       m_led.setData(m_ledBuffer);
      }
      else{
        timer.restart();
      }
  }

  @Override
  public void teleopInit()
  {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null)
    {
      m_autonomousCommand.cancel();
    }
    m_robotContainer.setDriveMode();
    m_robotContainer.setMotorBrake(true);

    timer.reset();
    timer.start();

    
  }

  /**
   * This function is called periodically during operator control.
   */
  AHRS ahrs;
  Joystick stick;
  
  @Override
  public void teleopPeriodic()
  {
   
    /*double gyro = ahrs.getYaw();
    SmartDashboard.putNumber(   "Velocity_X",           ahrs.getVelocityX());
    SmartDashboard.putNumber(   "Velocity_Y",           ahrs.getVelocityY());
    SmartDashboard.putNumber(   "IMU_Yaw",              ahrs.getYaw());
    SmartDashboard.putNumber(   "Gyro Math",              gyro);*/

    //Xbox Controller Rumble
    Double RumbleY;
    Double RumbleX;
    RumbleY = Math.abs(MathUtil.applyDeadband(driverXbox.getLeftY(), .1));
    RumbleX = Math.abs(MathUtil.applyDeadband(driverXbox.getLeftX(), .1));
    driverXbox.getHID().setRumble(RumbleType.kRightRumble, 0.25 * RumbleX + RumbleY);

    SmartDashboard.putString(   "LedColor",        "Green");

    
    Speedled = (int) ((Controls.swervespeedy / Controls.speedmax) / 255);

    if (Speedled > 0){
      for (var i = 30; i < 60; i++) {
        // Sets the specified LED to the RGB values for OFF
        m_ledBuffer.setRGB(i, Speedled, 0, 0);
     }
     
     m_led.setData(m_ledBuffer);
    }
    else {
      for (var i = 30; i < 60; i++) {
        // Sets the specified LED to the RGB values for Green
        m_ledBuffer.setRGB(i, 0, 0, 0);
     }
     
     m_led.setData(m_ledBuffer);
    }

    //LED Blink
    //var i = 0; i < LedFlash; i++
    if (timer.get() < 0.5){
      for (var i = 0; i < LedFlash; i++) {
        // Sets the specified LED to the RGB values for OFF
        m_ledBuffer.setRGB(i, 0, 0, 0);
     }
     
     m_led.setData(m_ledBuffer);
    }
    else if (timer.get() < 1){
      for (var i = 0; i < LedFlash; i++) {
        // Sets the specified LED to the RGB values for Green
        m_ledBuffer.setRGB(i, 0, 255, 0);
     }
     
     m_led.setData(m_ledBuffer);
    }
    else{
      timer.restart();
    }
  }
  

  @Override
  public void testInit()
  {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
    try
    {
      new SwerveParser(new File(Filesystem.getDeployDirectory(), "swerve"));
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic()
  {
  }

  /**
   * This function is called once when the robot is first started up.
   */
  @Override
  public void simulationInit()
  {
  }

  /**
   * This function is called periodically whilst in simulation.
   */
  @Override
  public void simulationPeriodic()
  {
    
  }
}
