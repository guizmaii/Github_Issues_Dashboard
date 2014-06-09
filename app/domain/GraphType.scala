package domain


/**
 * Enumération des types de graphs.
 */
sealed abstract class GraphType(value: String)

case object G1Type extends GraphType("g1")
case object G2Type extends GraphType("g2")
case object G3Type extends GraphType("g3")
case object G4Type extends GraphType("g4")



