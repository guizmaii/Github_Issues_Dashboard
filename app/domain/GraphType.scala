package domain


/**
 * Enumération des types de graphs.
 */
sealed case class GraphType(num: Int)

case object G1 extends GraphType(1)
case object G2 extends GraphType(2)
case object G3 extends GraphType(3)
case object G4 extends GraphType(4)



