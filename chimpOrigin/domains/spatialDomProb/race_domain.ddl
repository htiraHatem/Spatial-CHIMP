##################
# Reserved words #
################################################################
# HybridHTNDomain                                              #
# MaxArgs                                                      #
# :operator                                                    #
# :method                                                      #
# Head                                                         #
# Pre                                                          #
# Add                                                          #
# Del                                                          #
# Sub                                                          #
# Constraint                                                   #
# Ordering                                                     #
# Type                                                         #
# Values                                                       #
# StateVariable                                                #
# FluentResourceUsage                                          #
# Param                                                        #
# ResourceUsage                                                #
# Usage                                                        #
# Resource                                                     #
# Fluent                                                       #
#                                                              #
##   All AllenIntervalConstraint types                         #
##   '[' and ']' should be used only for constraint bounds     #
##   '(' and ')' are used for parsing                          #
#                                                              #
################################################################

(HybridHTNDomain RACEDomain)

(MaxArgs 5)

(MaxIntegerArgs 2)

(PredicateSymbols On RobotAt Holding HasArmPosture HasTorsoPosture
  Connected Type enoughDistance isCollision !shift_object !pick_up_two_object serve_an_empty_dish
  !move_base !move_base_blind !place_object !pick_up_object
  !move_arm_to_side !move_arms_to_carryposture !tuck_arms !move_torso
  !observe_objects_on_area
  adapt_torso torso_assume_driving_pose adapt_arms arms_assume_driving_pose
  drive_robot move_both_arms_to_side assume_manipulation_pose
  leave_manipulation_pose grasp_object get_object put_object
  move_object serve_coffee_to_guest arm_to_side
  serve_coffee_to_guest_test assume_manipulation_pose_wrapper
  not_test
  Future
  Eternity
  objPos)

(Resource objManCapacity 1)
(Resource armManCapacity 1)
(Resource navigationCapacity 1)
(Resource leftArm1 1)
(Resource rightArm1 1)

(Resource objMoveCapacity 2)

(StateVariable RobotAt 2 n)
(StateVariable HasArmPosture 1 leftArm1 rightArm1)
(StateVariable Holding 1 leftArm1 rightArm1)
(StateVariable On 1 mug1 mug2 mug3)

################################
####  OPERATORS ################

# MOVE_BASE
(:operator
 (Head !move_base(?toArea))
 (Pre p1 RobotAt(?fromArea))
 (Constraint OverlappedBy(task,p1))
 (Add e1 RobotAt(?toArea))
 (Constraint Meets(task,e1))
 (Del p1)
 (ResourceUsage navigationCapacity 1)
)

# MOVE_BASE_BLIND   PreArea to ManArea
(:operator
 (Head !move_base_blind(?mArea))
 (Pre p1 RobotAt(?preArea))
 (Del p1)
 (Pre p2 Connected(?plArea ?mArea ?preArea))
 (Constraint Duration[4000,INF](task))
 (Add e1 RobotAt(?mArea))
 (Constraint Meets(task,e1))
 (Constraint Meets(p1,task))
 (ResourceUsage 
    (Usage navigationCapacity 1))
)

# MOVE_BASE_BLIND   ManArea to PreArea
(:operator
 (Head !move_base_blind(?preArea))
 (Pre p1 RobotAt(?mArea))       # TODO use type restriction
 (Pre p2 Connected(?plArea ?mArea ?preArea))
 (Constraint Duration[4000,INF](task))
 (Add e1 RobotAt(?preArea))
 (Constraint Meets(task,e1))
 (Constraint Meets(p1,task))
 (Del p1)
 (ResourceUsage 
    (Usage navigationCapacity 1))
)

# TUCK_ARMS
(:operator
 (Head !tuck_arms(?leftGoal ?rightGoal))
 (Pre p1 HasArmPosture(?leftArm ?oldLeft))
 (Pre p2 HasArmPosture(?rightArm ?oldRight))
 (Del p1)
 (Del p2)
 (Add e1 HasArmPosture(?leftArm ?leftGoal))
 (Add e2 HasArmPosture(?rightArm ?rightGoal))
 (Values ?leftArm leftArm1)
 (Values ?rightArm rightArm1)
 (Values ?leftGoal ArmTuckedPosture ArmUnTuckedPosture)
 (Values ?rightGoal ArmTuckedPosture ArmUnTuckedPosture)
 (ResourceUsage 
    (Usage armManCapacity 1))
 (Constraint Duration[4000,INF](task))
)


# MOVE_TORSO
(:operator
 (Head !move_torso(?newPosture))
 (Pre p1 HasTorsoPosture(?oldPosture))
 (Constraint OverlappedBy(task,p1))
 (Del p1)
 (Add e1 HasTorsoPosture(?newPosture))
 (Constraint Duration[4000,INF](task))
)

# PICK_UP_OBJECT
(:operator
 (Head !pick_up_object(?obj ?arm))
 (Pre p1 On(?obj ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
 (Pre p4 Holding(?arm ?nothing))
 (Values ?nothing nothing)
 (Del p1)
 (Del p4)
 (Add e1 Holding(?arm ?obj))
  
 (Constraint OverlappedBy(task,p1))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p3))
 (Constraint OverlappedBy(task,p4))
 (Constraint Meets(p4,e1))
 (Constraint Duration[4000,INF](task))
 
 (DelSC (At) (?obj))
 
 (ResourceUsage 
    (Usage armManCapacity 1))
 (ResourceUsage 
    (Usage objManCapacity 1))
)

# if we hv collision, we hold every object in different hand
(:operator 10
 (Head !pick_up_two_object(?obj1 ?arm ?obj2 ?otherArm))
 (Pre p1 On(?obj2 ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
 (Pre p4 Holding(?arm ?nothing))
 (Values ?arm leftArm1)
 (Pre p5 Holding(?otherArm ?nothing))
 (Values ?otherArm rightArm1)
 (Values ?nothing nothing)
 (Pre p6 isCollision(?obj1 ?obj2 ?true))
 (Values ?true true)
 
 (Del p1)
 (Del p4)
 (Del p5)
 
 (Add e1 Holding(?arm ?obj1)) #top obj should be held first
 (Add e2 Holding(?otherArm ?obj2))
 
 (Constraint Meets(p4,e1))
 (Constraint Meets(p5,e2))
 (Constraint OverlappedBy(task,p1))
 (Constraint OverlappedBy(task,p4))
 (Constraint OverlappedBy(task,p5))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p3))
 (Constraint Duration[5000,INF](task))

 (DelSC (?obj1,?obj2)) #delete binary rel between the two objects and it's assertion
 (DelSC (At) (?obj1)) #delete At: obj not anymore at that position
 (DelSC (At) (?obj2))
 
 (ResourceUsage 
    (Usage armManCapacity 1))
 (ResourceUsage 
    (Usage objManCapacity 1))
)

(:operator 10
 (Head !pick_up_object(?obj1 ?arm))
 (Pre p1 On(?obj ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
 (Pre p4 Holding(?arm ?nothing))
 (Values ?nothing nothing)
 (Pre p5 isCollision(?obj1 ?obj2 ?false))
 (Values ?false false)
 (Del p1)
 (Del p4)
 (Add e1 Holding(?arm ?obj1))
 (Constraint OverlappedBy(task,p1))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p3))
 (Constraint OverlappedBy(task,p4))
 (Constraint Meets(p4,e1))
 (Constraint Duration[4000,INF](task))
 
 (DelSC (At) (?obj2)) #delete At: obj not anymore at that position and delete assertion
 
 (ResourceUsage 
    (Usage armManCapacity 1))
 (ResourceUsage 
    (Usage objManCapacity 1))
)

# SHIFT_OBJECT
(:operator
 (Head !shift_object(?objFork ?objDish ?arm)(?objFork ?objDish))
 (Pre p1 Holding(?arm ?nothing))
 (Values ?nothing nothing)
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?plArea ?mArea ?preArea))
 (Pre p4 HasArmPosture(?arm ?armPosture))
 (Values ?armPosture ArmToSidePosture)
 (Pre p5 enoughDistance(?objDish ?objKnife ?objFork ?false))
 (Values ?false false)
 (Del p5)
 
 (SC ?objFork = ?objDish + 5) #shift the fork based on the required size of the Dish
 (Add e1 enoughDistance(?objDish ?objKnife ?objFork ?true))
 (Values ?true true)
 (Constraint Finishes(task,e1))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p5))
 (Constraint Duration[5000,INF](task)) 

)

# PLACE_OBJECT


# place mug without checking distance
(:operator 10
 (Head !place_object(?obj ?arm ?plArea))
 (Pre p1 Holding(?arm ?obj))
 (Values ?obj mug mug1)
 
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?plArea ?mArea ?preArea))
 (Pre p4 HasArmPosture(?arm ?armPosture))
 (Values ?armPosture ArmToSidePosture)
 (Del p1)
 (Add e1 Holding(?arm ?nothing))
 (Values ?nothing nothing)
 (Add e2 On(?obj ?plArea))
 (SC (Equals,Equals) (?obj,?mug)) #add Assertion mug1 ->Mug
 (Values ?mug mug)
 
 (Constraint OverlappedBy(task,p1))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p3))
 (Constraint Meets(p1,e1))
 (Constraint Duration[4000,INF](task)) 
)
# place dish
(:operator 10
 (Head !place_object(?obj ?arm ?plArea))
 (Pre p1 Holding(?arm ?obj))
 (Values ?obj Dish dish1)
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?plArea ?mArea ?preArea))
 (Pre p4 HasArmPosture(?arm ?armPosture))
 (Values ?armPosture ArmToSidePosture)

 (Del p1)
 (Add e1 Holding(?arm ?nothing))
 (Values ?nothing nothing)
 (Add e2 On(?obj ?plArea))
 
 (SC (Equals,Equals) (?obj,?Dish)) #add Assertion dish ->dish1
 (Values ?Dish Dish)
 #(SC (During,During) (?obj,?plArea)) # obj on plArea , done with assertion auto
 
 (Constraint OverlappedBy(task,p1))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p3))
 (Constraint Meets(p1,e1))
 (Constraint Duration[4000,INF](task)) 
 (ResourceUsage 
    (Usage leftArm1ManCapacity 1)
    (Param 2 leftArm1))
 (ResourceUsage 
    (Usage rightArm1ManCapacity 1)
    (Param 2 rightArm1))
 (ResourceUsage 
    (Usage objManCapacity 1))
)

#place knife
(:operator 10
 (Head !place_object(?obj ?arm ?plArea))
 (Pre p1 Holding(?arm ?obj))
 (Values ?obj knife knife1 knife2)
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?plArea ?mArea ?preArea))
 (Pre p4 HasArmPosture(?arm ?armPosture))
 (Values ?armPosture ArmToSidePosture)

 (Del p1)
 (Add e1 Holding(?arm ?nothing))
 (Values ?nothing nothing)
 (Add e2 On(?obj ?plArea))
 (SC (Equals,Equals) (?obj,?knife)) #add Assertion knife ->knife1
 (Values ?knife knife)
 
 (Constraint OverlappedBy(task,p1))
 (Constraint During(task,p2)) # robot has to be at the table the whole time
 (Constraint During(task,p3))
 (Constraint Meets(p1,e1))
 (Constraint Duration[4000,INF](task)) 
 (ResourceUsage 
    (Usage leftArm1ManCapacity 1)
    (Param 2 leftArm1))
 (ResourceUsage 
    (Usage rightArm1ManCapacity 1)
    (Param 2 rightArm1))
 (ResourceUsage 
    (Usage objManCapacity 1))
)
# MOVE_ARM_TO_SIDE
(:operator
 (Head !move_arm_to_side(?arm))
 (Pre p1 HasArmPosture(?arm ?oldPosture))
 (Del p1)
 (Add e1 HasArmPosture(?arm ?newPosture))
 (Values ?oldPosture ArmUnTuckedPosture ArmCarryPosture ArmUnnamedPosture)
 (Values ?newPosture ArmToSidePosture)
 (ResourceUsage 
    (Usage armManCapacity 1))
 (Constraint Duration[4000,INF](task))
 (Constraint OverlappedBy(task,p1))
 (Constraint Overlaps(task,e1))
 )

(:operator # if arm is tucked the other must not be tucked
 (Head !move_arm_to_side(?arm)) # leftArm1
 (Pre p1 HasArmPosture(?arm ?oldPosture))
 (Pre p2 HasArmPosture(?otherArm ?otherPosture))
 (Del p1)
 (Add e1 HasArmPosture(?arm ?newPosture))
 (Values ?arm leftArm1)
 (Values ?otherArm rightArm1)
 (Values ?oldPosture ArmTuckedPosture)
 (Values ?otherPosture ArmUnTuckedPosture ArmCarryPosture ArmUnnamedPosture ArmToSidePosture)
 (Values ?newPosture ArmToSidePosture)
 (ResourceUsage 
    (Usage armManCapacity 1))
 (Constraint Duration[4000,INF](task))
 (Constraint OverlappedBy(task,p1))
 (Constraint Overlaps(task,e1))
)

(:operator # if arm is tucked the other must not be tucked
 (Head !move_arm_to_side(?arm)) # rightArm1
 (Pre p1 HasArmPosture(?arm ?oldPosture))
 (Pre p2 HasArmPosture(?otherArm ?otherPosture))
 (Del p1)
 (Add e1 HasArmPosture(?arm ?newPosture))
 (Values ?arm rightArm1)
 (Values ?otherArm leftArm1)
 (Values ?oldPosture ArmTuckedPosture)
 (Values ?otherPosture ArmUnTuckedPosture ArmCarryPosture ArmUnnamedPosture ArmToSidePosture)
 (Values ?newPosture ArmToSidePosture)
 (ResourceUsage 
    (Usage armManCapacity 1))
 (Constraint Duration[4000,INF](task))
 (Constraint OverlappedBy(task,p1))
 (Constraint Overlaps(task,e1))
)

# MOVE_ARMS_TO_CARRYPOSTURE
(:operator
 (Head !move_arms_to_carryposture())
 (Pre p1 HasArmPosture(?leftArm ?oldLeft))
 (Pre p2 HasArmPosture(?rightArm ?oldRight))
 (Pre p3 HasTorsoPosture(?torsoPosture))
 (Del p1)
 (Del p2)
 (Add e1 HasArmPosture(?leftArm ?newPosture))
 (Add e2 HasArmPosture(?rightArm ?newPosture))
 (Values ?leftArm leftArm1)
 (Values ?rightArm rightArm1)
 (Values ?newPosture ArmCarryPosture)
 (Values ?torsoPosture TorsoUpPosture TorsoMiddlePosture)
 (ResourceUsage 
    (Usage armManCapacity 1))    
 (Constraint Duration[4000,INF](task))
 (Constraint OverlappedBy(task,p1))
 (Constraint OverlappedBy(task,p2))
 (Constraint Overlaps(task,e1))
 (Constraint Overlaps(task,e2))
 )

# OBSERVE_OBJECTS_ON_AREA
(:operator
 (Head !observe_objects_on_area(?plArea))
 (Pre p1 RobotAt(?robotArea))    
 (Pre p2 Connected(?plArea ?robotArea ?preArea))
 (Constraint During(task,p1))
 (Constraint During(task,p2))
 (Constraint Duration[4000,INF](task))
)

################################

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

#################################

(:method
 (Head adapt_torso(?newPose))
 (Pre p1 HasTorsoPosture(?oldPose))
 (VarDifferent ?newPose ?oldPose) 
 (Sub s1 !move_torso(?newPose))
 (Constraint Equals(s1,task))
 )

(:method
 (Head adapt_torso(?posture))
 (Pre p1 HasTorsoPosture(?posture))
 (Constraint Duration[10,INF](task))
 (Constraint During(task,p1))
 )

###

(:method   # holding nothing
 (Head torso_assume_driving_pose())
  (Pre p1 Holding(?leftArm ?nothing))
  (Pre p2 Holding(?rightArm ?nothing))
  (Values ?nothing nothing)
  (Values ?leftArm leftArm1)
  (Values ?rightArm rightArm1)
  (Sub s1 adapt_torso(?newPose))
  (Values ?newPose TorsoDownPosture)
  (Constraint Equals(s1,task))
)

(:method # holding something
 (Head torso_assume_driving_pose())
  (Pre p1 Holding(?arm ?obj))
  (NotValues ?obj nothing)
  (Sub s1 adapt_torso(?newPose))
  (Values ?newPose TorsoMiddlePosture)
  (Constraint Equals(s1,task))
)

###

(:method  # Arms already there. Nothing to do.
 (Head adapt_arms(?posture))
 (Pre p1 HasArmPosture(?leftArm ?posture))
 (Pre p2 HasArmPosture(?rightArm ?posture))
 (Values ?leftArm leftArm1)
 (Values ?rightArm rightArm1)
 (Constraint Duration[3,INF](task))
 (Constraint During(task,p1))
 (Constraint During(task,p2))
 )

(:method  # tuck arms
 (Head adapt_arms(?posture))
 (Pre p1 HasArmPosture(?arm ?currentposture))
 (Values ?posture ArmTuckedPosture)
 (NotValues ?currentposture ArmTuckedPosture)
 (Sub s1 !tuck_arms(?posture ?posture))
 (Constraint Equals(s1,task))
)

(:method  # to carryposture
 (Head adapt_arms(?posture))
 (Pre p1 HasArmPosture(?arm ?currentposture))
 (Values ?posture ArmCarryPosture)
 (NotValues ?currentposture ArmCarryPosture)
 (Sub s1 !move_arms_to_carryposture())
 (Constraint Equals(s1,task))
 )

###

(:method    # holding nothing
 (Head arms_assume_driving_pose())
  (Pre p1 Holding(?leftArm ?nothing))
  (Pre p2 Holding(?rightArm ?nothing))
  (Values ?nothing nothing)
  (Values ?leftArm leftArm1)
  (Values ?rightArm rightArm1)
  (Sub s1 adapt_arms(?newPose)) 
  (Values ?newPose ArmTuckedPosture)
  (Constraint Equals(s1,task))
)

(:method    # holding something
 (Head arms_assume_driving_pose())
  (Pre p1 Holding(?arm ?obj))
  (NotValues ?obj nothing)
  (Sub s1 adapt_arms(?newPose))
  (Values ?newPose ArmCarryPosture)
  (Constraint Equals(s1,task))
)


### DRIVE_ROBOT

(:method    # already there
 (Head drive_robot(?toArea))
  (Pre p1 RobotAt(?toArea))
  (Constraint During(task,p1))
  (Constraint Duration[10,INF](task))
)


(:method    # not at manipulationarea
 (Head drive_robot(?toArea))
 (Pre p1 RobotAt(?fromArea))
 (VarDifferent ?toArea ?fromArea)
 (NotType ?fromArea ManipulationArea)
 (Sub s1 torso_assume_driving_pose())
 (Constraint Starts(s1,task))
 (Sub s2 arms_assume_driving_pose())
 (Sub s3 !move_base(?toArea))
 (Ordering s1 s2)
 (Ordering s2 s3)
 (Constraint Before(s1,s3))
 (Constraint Before(s2,s3))
)


(:method    # at manipulationarea
 (Head drive_robot(?toArea))
 (Pre p1 RobotAt(?fromArea))
 (VarDifferent ?toArea ?fromArea)
 (Type ?fromArea ManipulationArea)
 (Pre p2 Connected(?plArea ?fromArea ?preArea))
 (Sub s0 !move_base_blind(?preArea))
 (Constraint Starts(s0,task))
 (Sub s1 torso_assume_driving_pose())
 (Sub s2 arms_assume_driving_pose())
 (Sub s3 !move_base(?toArea))
 (Constraint Finishes(s3,task))
 (Ordering s0 s1)
 (Ordering s1 s2)
 (Ordering s2 s3)
 (Constraint Before(s0,s1))
 (Constraint Before(s0,s2))
 (Constraint Before(s1,s3))
 (Constraint Before(s2,s3))
)

# MOVE_BOTH_ARMS_TO_SIDE
(:method         # both are tucked
 (Head move_both_arms_to_side())

  (Pre p1 HasArmPosture(?leftArm ?oldLeftPosture))
  (Pre p2 HasArmPosture(?rightArm ?oldRightPosture))
  (Values ?oldLeftPosture ArmTuckedPosture)
  (Values ?oldRightPosture ArmTuckedPosture)
  (Values ?leftArm leftArm1)
  (Values ?rightArm rightArm1)
  (Sub s1 !tuck_arms(?lUntuckedPosture ?rUntuckedPosture))
  (Values ?lUntuckedPosture ArmUnTuckedPosture)
  (Values ?rUntuckedPosture ArmUnTuckedPosture)
  (Sub s2 !move_arm_to_side(?leftArm))
  (Sub s3 !move_arm_to_side(?rightArm))
  (Ordering s1 s2)
  (Ordering s2 s3)
  (Constraint Starts(s1,task))
  (Constraint Before(s1,s2))
  (Constraint Before(s1,s3))
)


# don't untuck if not both are tucked
(:method 
 (Head move_both_arms_to_side())
  (Pre p1 HasArmPosture(?arm ?oldPosture)) # new
  (NotValues ?oldPosture ArmTuckedPosture)
  (Values ?arm leftArm1 rightArm1)
  (Values ?leftArm leftArm1)
  (Values ?rightArm rightArm1)
  (Sub s1 arm_to_side(?leftArm))
  (Sub s2 arm_to_side(?rightArm))
  (Ordering s1 s2)
  (Constraint Before(s1,s2))
)

# arm is not at side
(:method 
 (Head arm_to_side(?arm))
  (Pre p1 HasArmPosture(?arm ?armPosture))
  (NotValues ?armPosture ArmToSidePosture)
  (Sub s1 !move_arm_to_side(?arm))
  (Constraint Equals(s1,task))
)

# arm is at side
(:method 
 (Head arm_to_side(?arm))
  (Pre p1 HasArmPosture(?arm ?armPosture))
  (Values ?armPosture ArmToSidePosture)
  (Constraint During(task,p1))
)

### ASSUME_MANIPULATION_POSE
# 1. only adapt torso
(:method 10         
 (Head assume_manipulation_pose(?manArea))
  (Pre p1 HasArmPosture(?leftArm ?leftPosture))
  (Pre p2 HasArmPosture(?rightArm ?rightPosture))
  (Pre p3 RobotAt(?manArea))
  (Values ?leftArm leftArm1)
  (Values ?rightArm rightArm1)
  (Values ?leftPosture ArmToSidePosture)
  (Values ?rightPosture ArmToSidePosture)
  (Values ?torsoPosture TorsoUpPosture)
  (Sub s1 adapt_torso(?torsoUpPosture))
  (Constraint Equals(s1,task))
)


# 2. standard behaviour
(:method 
 (Head assume_manipulation_pose(?manArea))
  (Pre p1 RobotAt(?preArea))
  (Pre p2 Connected(?plArea ?manArea ?preArea))
  (Sub s1 adapt_torso(?torsoUpPosture))
  (Values ?torsoUpPosture TorsoUpPosture)
  (Sub s2 move_both_arms_to_side())
  (Sub s3 !move_base_blind(?manArea))
  (Ordering s1 s2)
  (Ordering s2 s3)
  (Constraint Before(s1,s3))
  (Constraint Before(s2,s3))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s3,task))
)

# first move back to preArea
(:method 8
 (Head assume_manipulation_pose(?manArea))
  (Pre p1 RobotAt(?manArea))
  (Pre p2 Connected(?plArea ?manArea ?preArea))
  (Pre p3 HasArmPosture(?arm ?armPosture))
  (NotValues ?armPosture ArmToSidePosture)
  (Sub s0 !move_base_blind(?preArea))
  (Sub s1 adapt_torso(?torsoUpPosture))
  (Values ?torsoUpPosture TorsoUpPosture)
  (Sub s2 move_both_arms_to_side())
  (Sub s3 !move_base_blind(?manArea))
  (Ordering s0 s1)
  (Ordering s1 s2)
  (Ordering s2 s3)
  (Constraint Starts(s0,task))
  (Constraint Finishes(s3,task))
  (Constraint Before(s0,s1))
  (Constraint Before(s0,s2))
  (Constraint Before(s1,s3))
  (Constraint Before(s2,s3))
)

### LEAVE_MANIPULATION_POSE
(:method 
 (Head leave_manipulation_pose(?manArea))
  (Pre p1 RobotAt(?manArea))
  (Pre p2 Connected(?plArea ?manArea ?preArea))
  (Sub s1 !move_base_blind(?preArea))
  (Constraint Equals(s1,task))
 )

### GRASP_OBJECT
(:method 
  (Head grasp_object(?object))
  (Pre p1 RobotAt(?preArea))  # checked in assume_manipulation_pose
  (Pre p2 Connected(?plArea ?manArea ?preArea))
  (Pre p3 On(?object ?plArea))
  (Values ?arm leftArm1 rightArm1) # TODO use type or leave it unground
  (Sub s1 assume_manipulation_pose(?manArea))
#  (Sub s2 !observe_objects_on_area(?plArea))
  (Sub s3 !pick_up_object(?object ?arm))
#  (Ordering s1 s2)
#  (Ordering s2 s3)
  (Ordering s1 s3)
  (Constraint Before(s1, s3))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s3,task))
)

### GET_OBJECT

(:method 10
  (Head get_object(?object)) 
  (Pre p0 Connected(?plArea ?manArea ?preArea))
  (Pre p1 On(?object ?plArea))
  (Sub s1 assume_manipulation_pose(?manArea))
#  (Sub s2 !observe_objects_on_area(?plArea))
  (Sub s2 !pick_up_object(?object ?arm))
  (Ordering s1 s2)
  (Constraint Before(s1,s2))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s2,task))
)

#  Robot is not at preArea
(:method 
  (Head get_object(?object)) 
  (Pre p1 RobotAt(?robotArea))
  (Pre p2 Connected(?plArea ?manArea ?preArea))
  (Pre p3 On(?object ?plArea))
  (VarDifferent ?robotArea ?preArea) 
  (Sub s1 drive_robot(?preArea))
  (Sub s2 assume_manipulation_pose(?manArea))
#  (Sub s3 !observe_objects_on_area(?plArea))
  (Sub s3 !pick_up_object(?object ?arm))
  (Ordering s1 s2)
  (Ordering s2 s3)
  (Constraint Before(s1,s2))
  (Constraint Before(s2,s3))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s3,task))
)

### PUT_OBJECT
# 1.0 not at premanipulationarea or manipulationarea -> drive (no distance check)
(:method 
  (Head put_object(?object ?plArea))
  (Pre p1 Holding(?arm ?object))
  (Pre p2 RobotAt(?robotArea))
  (Pre p3 Connected(?plArea ?manArea ?preArea))
  (VarDifferent ?robotArea ?preArea)
  (VarDifferent ?robotArea ?manArea)
  (Sub s1 drive_robot(?preArea))
  (Sub s2 assume_manipulation_pose(?manArea))
  (Sub s3 !place_object(?object ?arm ?plArea))
  (Ordering s1 s2)
  (Ordering s2 s3)
  (Constraint Before(s1,s2))
  (Constraint Before(s2,s3))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s3,task))
)

# 1.1 not at premanipulationarea or manipulationarea -> drive (enoughDistance: false)
(:method 10
  (Head put_object(?objDish ?plArea))
  (Pre p1 Holding(?arm ?objDish))
  (Values ?objDish dish1)
 
  (Pre p2 RobotAt(?robotArea))
  (Pre p3 Connected(?plArea ?manArea ?preArea))
  (VarDifferent ?robotArea ?preArea)
  (VarDifferent ?robotArea ?manArea)
  (Pre p4 enoughDistance(?objDish ?objKnife ?objFork ?false))
  (Values ?false false)
  (Pre p5 Holding(?armEmpty ?nothing))
  (Values ?nothing nothing)  
  
  (Sub s1 drive_robot(?preArea))
  (Sub s2 assume_manipulation_pose(?manArea))
  (Sub s3 !shift_object(?objFork ?objDish ?armEmpty)) 
  (Sub s4 !place_object(?objDish ?arm ?plArea))
  
  (Ordering s1 s2)
  (Ordering s2 s3)
  (Ordering s3 s4)
  (Constraint Before(s1,s2))
  (Constraint Before(s2,s3))
  (Constraint Before(s3,s4))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s4,task))
)

# 2.0 at premanipulationarea without checking enoughSpace
(:method 
  (Head put_object(?object ?plArea))
  (Pre p1 Holding(?arm ?object))
  (Pre p2 RobotAt(?preArea))
  (Pre p3 Connected(?plArea ?manArea ?preArea))
  (Sub s1 assume_manipulation_pose(?manArea))
  (Sub s2 !place_object(?object ?arm ?plArea))

  (Ordering s1 s2)
  (Constraint Starts(s1,task))
  (Constraint Finishes(s2,task))
  (Constraint Before(s1,s2))
)
# 2.1 at premanipulationarea if enough space
(:method 
  (Head put_object(?object ?plArea))
  (Pre p1 Holding(?arm ?object))
  (Pre p2 RobotAt(?preArea))
  (Pre p3 Connected(?plArea ?manArea ?preArea))
  (Pre p4 enoughDistance(?object ?object1 ?object2 ?true))
  (Values ?true true)
  (Sub s1 assume_manipulation_pose(?manArea))
  (Sub s2 !place_object(?object ?arm ?plArea))

  (Ordering s1 s2)
  (Constraint Starts(s1,task))
  (Constraint Finishes(s2,task))
  (Constraint Before(s1,s2))
)

# 2.2 at premanipulationarea if not enough space and one arm should be empty to use in shifting
(:method 10
  (Head put_object(?object ?plArea))
  (Pre p1 Holding(?arm ?object))
  (Pre p2 RobotAt(?preArea))
  (Pre p3 Connected(?plArea ?manArea ?preArea))
  (Pre p4 enoughDistance(?objDish ?objKnife ?objFork ?false))
  (Values ?false false)
  (Pre p5 Holding(?armEmpty ?nothing))
  (Values ?nothing nothing)  
  
  (Sub s1 assume_manipulation_pose(?manArea))
  (Sub s2 !shift_object(?objFork ?objDish ?armEmpty)) 
  (Sub s3 !place_object(?object ?arm ?plArea))
  
  (Ordering s1 s2)
  (Ordering s2 s3)
  (Constraint Starts(s1,task))
  (Constraint Finishes(s3,task))
  (Constraint Before(s1,s2))
  (Constraint Before(s2,s3))
)

# 3. at manipulationarea
(:method 
  (Head put_object(?object ?plArea))
  (Pre p1 Holding(?arm ?object))
  (Pre p2 RobotAt(?manArea))
  #(Pre p3 Connected(?plArea ?manArea ?preArea))
  (Sub s1 !place_object(?object ?arm ?plArea))
  (Constraint Equals(s1,task))
)

### MOVE_OBJECT
(:method 
  (Head move_object(?object ?toArea))
  (Pre p1 On(?object ?fromArea))
  (Sub s1 get_object(?object))
  (Sub s2 put_object(?object ?toArea))
  (Ordering s1 s2)
  (Constraint Before(s1,s2))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s2,task))
  (Constraint Duration[20000,INF](task))
  (ResourceUsage (Usage objMoveCapacity 1))
)

### MOVE_OBJECT when the object is already held
(:method 
  (Head move_object(?object ?toArea))
  (Pre p1 Holding(?arm ?object))
  (Sub s2 put_object(?object ?toArea))
  (Constraint Finishes(s2,task))
  (Constraint Duration[20000,INF](task))
  (ResourceUsage (Usage objMoveCapacity 1))
)
### SERVE_COFFEE_TO_GUEST
(:method 
  (Head serve_single_coffee_to_guest(?guest))
  (Pre p1 On(?object ?fromArea))
  (Values ?fromArea placingAreaEastLeftTable1 placingAreaWestLeftTable1 placingAreaNorthLeftTable2 placingAreaSouthLeftTable2)
  (Values ?leftArm leftArm1)
  (Sub s1 get_object_w_arm(?object ?leftArm))
  (Sub s2 put_object(?object ?toArea))
  (Ordering s1 s2)
  (Constraint Before(s1,s2))
  (Constraint Starts(s1,task))
  (Constraint Finishes(s2,task))
  )

#########enough distance
#### hold an empty dish if there's a collision
(:method
  (Head serve_an_empty_dish(?obj2 ?toArea))
 (Pre p1 On(?obj2 ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
  (Values ?fromArea placingAreaCounter1)
 (Pre p4 Holding(?arm ?nothing))
 (Values ?arm leftArm1)
 (Pre p5 Holding(?otherArm ?nothing))
 (Values ?otherArm rightArm1)
 (Values ?nothing nothing)
 (Pre p6 isCollision(?obj1 ?obj2 ?true))
 (Values ?true true)  
 (Pre p7 Connected(?toArea ?mArea1 ?preArea1))
   (Values ?toArea placingAreaTable1)
 (Pre p8 enoughDistance(?obj2 ?objKnife ?objFork ?true))
 (Values ?true true)

 (Sub s1 !pick_up_two_object(?obj1 ?arm ?obj2 ?otherArm))
 (Sub s2 put_object(?obj1 ?fromArea))
 (Sub s3 drive_robot(?preArea1))
 (Sub s4 assume_manipulation_pose(?mArea1)) 
 (Sub s5 !place_object(?obj2 ?otherArm ?toArea))
 
 (Ordering s1 s2)
 (Ordering s2 s3)
 (Constraint Before(s1,s2))
 (Constraint Before(s2,s3))  
 (Constraint Before(s3,s4)) 
 (Constraint Before(s4,s5)) 
 (Constraint Starts(s1,task)) 
 (Constraint Finishes(s5,task))  

)

#### hold an empty dish if there's collision-free 
(:method 10
  (Head serve_an_empty_dish(?obj2 ?toArea))
 (Pre p1 On(?obj2 ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
  (Values ?fromArea placingAreaCounter1)
 (Pre p4 Holding(?arm ?nothing))
 (Values ?arm leftArm1)
 (Pre p5 Holding(?otherArm ?nothing))
 (Values ?otherArm rightArm1)
 (Values ?nothing nothing)
 (Pre p6 isCollision(?obj1 ?obj2 ?false))
 (Values ?false false)  
 (Pre p7 Connected(?toArea ?mArea1 ?preArea1))
   (Values ?toArea placingAreaTable1)
 (Pre p8 enoughDistance(?obj2 ?objKnife ?objFork ?true))
 (Values ?true true)

 (Sub s1 !pick_up_object(?obj2 ?otherArm))
 (Sub s2 move_object(?obj2 ?toArea))
 
 (Ordering s1 s2)
 (Constraint Before(s1,s2))
   
)
#########not enough distance
## collision
(:method
  (Head serve_an_empty_dish(?obj2 ?toArea))
 (Pre p1 On(?obj2 ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
  (Values ?fromArea placingAreaCounter1)
 (Pre p4 Holding(?arm ?nothing))
 (Values ?arm leftArm1)
 (Pre p5 Holding(?otherArm ?nothing))
 (Values ?otherArm rightArm1)
 (Values ?nothing nothing)
 (Pre p6 isCollision(?obj1 ?obj2 ?true))
 (Values ?true true)  
 (Pre p7 Connected(?toArea ?mArea1 ?preArea1))
   (Values ?toArea placingAreaTable1)
 (Pre p8 enoughDistance(?obj2 ?objKnife ?objFork ?false))
 (Values ?false false)
 
 (Sub s1 !pick_up_two_object(?obj1 ?arm ?obj2 ?otherArm))
 (Sub s2 put_object(?obj1 ?fromArea))
 (Sub s3 drive_robot(?preArea1))
 (Sub s4 assume_manipulation_pose(?mArea1)) 
 (Sub s5 !shift_object(?objFork ?obj2 ?otherArm)) 
 (Sub s6 !place_object(?obj2 ?otherArm ?toArea))

 (Ordering s1 s2)
 (Ordering s2 s3)
 (Constraint Before(s1,s2))
 (Constraint Before(s2,s3))  
 (Constraint Before(s3,s4)) 
 (Constraint Before(s4,s5)) 
 #(Constraint Meets(s5,s6)) 
 (Constraint Starts(s1,task)) 
 (Constraint Finishes(s6,task)) 
 

)
## no collision
(:method
  (Head serve_an_empty_dish(?obj2 ?toArea))
 (Pre p1 On(?obj2 ?fromArea))
 (Pre p2 RobotAt(?mArea))
 (Pre p3 Connected(?fromArea ?mArea ?preArea))
  (Values ?fromArea placingAreaCounter1)
 (Pre p4 Holding(?arm ?nothing))
 (Values ?arm leftArm1)
 (Pre p5 Holding(?otherArm ?nothing))
 (Values ?otherArm rightArm1)
 (Values ?nothing nothing)
 (Pre p6 isCollision(?obj1 ?obj2 ?false))
 (Values ?false false)  
 (Pre p7 Connected(?toArea ?mArea1 ?preArea1))
 (Values ?toArea placingAreaTable1)
 (Pre p8 enoughDistance(?obj2 ?objKnife ?objFork ?false))
 (Values ?false false)
 
 (Sub s1 !pick_up_object(?obj2 ?otherArm))
 (Sub s2 drive_robot(?preArea1))
 (Sub s3 assume_manipulation_pose(?mArea1)) 
 (Sub s4 !shift_object(?objFork ?obj2 ?otherArm)) 
 (Sub s5 !place_object(?obj2 ?otherArm ?toArea))

 (Ordering s1 s2)
 (Ordering s2 s3)
 (Constraint Before(s1,s2))
 (Constraint Before(s2,s3))  
 (Constraint Before(s3,s4)) 
 #(Constraint Before(s4,s5)) 
 (Constraint Starts(s1,task)) 
 (Constraint Finishes(s5,task)) 
)


#### serve a hot coffee with milk and sugar
(:method
  (Head serve_coffee_to_guest(?placingArea))
  (Pre p0 Type(?coffeetype ?coffee))
  (Values ?coffeetype Coffee)
 (Values ?placingArea placingAreaEastLeftTable1 placingAreaWestLeftTable1 placingAreaNorthLeftTable2 placingAreaSouthLeftTable2 placingAreaNorthRightTable2)
  (Pre p1 Type(?milktype ?milk))
  (Values ?milktype Milk)
#  (Pre p2 Type(?sugartype ?sugar))
#  (Values ?sugartype Sugar)
  (Values ?sugar sugarPot1 sugarPot2)
  (Sub s1 move_object(?coffee ?placingArea))
  (Sub s2 move_object(?milk ?placingArea))
  (Sub s3 move_object(?sugar ?placingArea))
  (Ordering s1 s2)
  (Ordering s2 s3)
  (Constraint Before(s1,s3))
  (Constraint Before(s2,s3))  
)

###################################################################
###################################################################
# ONLY FOR TESTING:
(:method
  (Head serve_coffee_to_guest_test(?placingArea ?pa2))

  (Pre p0 Type(?coffeetype ?coffee))
  (Values ?coffeetype Coffee)

 (Values ?placingArea placingAreaEastLeftTable1 placingAreaWestLeftTable1 placingAreaNorthLeftTable2 placingAreaSouthLeftTable2 placingAreaNorthRightTable2)

 (Values ?pa2 placingAreaEastLeftTable1 placingAreaWestLeftTable1 placingAreaNorthLeftTable2 placingAreaSouthLeftTable2 placingAreaNorthRightTable2)
  
  (Pre p1 Type(?milktype ?milk))
  (Values ?milktype Milk)

#  (Pre p2 Type(?sugartype ?sugar))
#  (Values ?sugartype Sugar)
  (Values ?sugar sugarPot1 sugarPot2)
  
  (Sub s1 move_object(?coffee ?placingArea))
  (Sub s2 move_object(?milk ?pa2))
#  (Sub s3 move_object(?sugar ?placingArea))

  (Ordering s1 s2)
#  (Constraint Starts(s1,task))
#(Constraint (s1,task))
#  (Ordering s1 s3)
#  (Ordering s2 s3)
#  (Constraint Finishes(s3,task))

#(Constraint Before(s1,s2))
#(Constraint Before(s2,s3))  
)