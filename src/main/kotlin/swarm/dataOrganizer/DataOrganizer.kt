package swarm.dataOrganizer

import swarm.Ref
import swarm.SwarmLocation

/**
 * Created by ian on 9/5/16.
 */
interface DataOrganizer {
    fun optimize(transitions: Map<RefPair, Int>): Map<Ref, SwarmLocation>
}

data class RefPair(val startRef: Ref, val endRef: Ref)