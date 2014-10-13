package me.sokolenko.microservice.domain.api

import groovy.transform.ToString

/**
 * Created by galak on 9/30/14.
 */
@ToString
class Challenge {

    def UUID uuid

    def String text

    def UUID authorUuid

    def Set<UUID> inviteeUuids

}
