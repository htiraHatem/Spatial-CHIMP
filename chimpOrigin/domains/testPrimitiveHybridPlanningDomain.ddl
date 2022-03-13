##################
# Reserved words #
#################################################################
#                                                               #
##   Head                                                        #
##   Resource                                                    #
##   Sensor                                                      #
##   ContextVariable                                             #
#   HTNOP                                                       #
##   PlanningOperator                                            #
#   HybridHTNDomain                                             #
##   Constraint                                                  #
##   RequiredState						#
##   AchievedState						#
##   RequriedResoruce						#
##   All AllenIntervalConstraint types                           #
##   '[' and ']' should be used only for constraint bounds       #
##   '(' and ')' are used for parsing                            #
#                                                               #
#
#   StateVariable
#   FluentResourceUsage
#   Param
#################################################################

(HybridHTNDomain TestDomain)

(MaxArgs 5)

(PredicateSymbols On RobotAt Holding HasArmPosture HasTorsoPosture
  Connected Type 
  Size At
  Duration After
  !move_base !move_base_blind !place_object !pick_up_object
  !move_arm_to_side !move_arms_to_carryposture !tuck_arms !move_torso
  !observe_objects_on_area
  adapt_torso torso_assume_driving_pose adapt_arms arms_assume_driving_pose
  drive_robot move_both_arms_to_side assume_manipulation_pose
  leave_manipulation_pose grasp_object get_object put_object
  move_object serve_coffee_to_guest arm_to_side
  serve_coffee_to_guest_test assume_manipulation_pose_wrapper
  !at_robot1_table1 !at_fork1_table1 !at_knife1_table1 !at_cup1_table1
  
  not_test
  Future
  Eternity)

(Resource leftArm1 1)
(Resource rightArm1 1)
(Resource leftArm1ManCapacity 1)
(Resource rightArm1ManCapacity 1)


(StateVariable On 1 mug1 mug2 mug3)
(StateVariable HasArmPosture 1 rightArm1 leftArm1)

(FluentResourceUsage 
  (Usage leftArm1 1) 
  (Fluent Holding)
  (Param 2 leftArm1)
)

(FluentResourceUsage 
  (Usage rightArm1 1) 
  (Fluent Holding)
  (Param 2 rightArm1)
)

(:method
 (Head drive(?toArea))
 (Constraint Duration[20,30](task))
 (Sub s1 assume_default_driving_pose())
# (Constraint During(s1,task))
 (Sub s2 !move_base(?toArea))
 (Ordering s1 s2)
)

(:method
 (Head assume_default_driving_pose())
 (Sub s1 !tuck_arms(armTuckedPosture armTuckedPosture))
 (Sub s2 !move_torso(torsoDownPosture))
# (Ordering s1 s2)
)

(:operator
 (Head !move_base(?toArea))
 (Pre p1 RobotAt(?fromArea))
 (Constraint StartedBy(task,req1))
 (Constraint OverlappedBy(task,req1))
 (Constraint Duration[5,INF](task))
 (Add e1 RobotAt(?toArea))
 (Del p1)
 (Add e9 On(?fromArea))
)

(:operator
 (Head !tuck_arms(?leftGoal ?rightGoal))
 (Pre p1 HasArmPosture(?leftArm ?oldLeft))
 (Pre p2 HasArmPosture(?rightArm ?oldRight))
 (Del p1)
 (Del p2)
 (Add e1 HasArmPosture(?leftArm ?leftGoal))
 (Add e2 HasArmPosture(?rightArm ?rightGoal))
 (Constraint Meets(task,e2))
 (Constraint Meets(task,e1))
# (Type ?oldLeft ArmPosture)
 (Values ?leftArm leftArm1)
 (Values ?rightArm rightArm1)
 (Constraint Equals(e1,e2))
 (Constraint Before(p1,e1))
)

(:operator
 (Head !move_torso(?newPosture))
 (Pre p1 HasTorsoPosture(?oldPosture))
 (Constraint OverlappedBy(task,p1))
 (Del p1)
 (Add e1 HasTorsoPosture(?newPosture))
)


(:operator
 (Head !pick_up_object(?obj ?arm))

 (Pre p1 On(?obj ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
 (Add e1 Holding(?obj ?arm))

# (Constraint StartedBy(task,req1))
# (Constraint OverlappedBy(task,req1))
# (Constraint Duration[5,INF](task))
 (Del p1)
 
 (ResourceUsage 
    (Usage leftArm1ManCapacity 1)
    (Param 2 leftArm1))
 (ResourceUsage 
    (Usage rightArm1ManCapacity 1)
    (Param 2 rightArm1))
)


#(:operator
# (Head op::!move_base(Area:?toArea Area:?fromArea))
# # TODO set negative
# (Pre p1 trixi::RobotAt(Area:?fromArea))
# (Constraint StartedBy(task,req1))
# (Constraint OverlappedBy(task,req1))
# (Constraint Duration[5,INF](task))
# (Add e1 trixi::RobotAt(Area:?toArea))
#)

