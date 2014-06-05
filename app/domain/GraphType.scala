package domain


/**
 * Enumération des types de graphs.
 */
sealed abstract class GraphType(value: String)

case object G1 extends GraphType("g1")
case object G2 extends GraphType("g2")
case object G3 extends GraphType("g3")
case object G4 extends GraphType("g4")



