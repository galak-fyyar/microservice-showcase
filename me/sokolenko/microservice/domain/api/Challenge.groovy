package me.sokolenko.microservice.domain.api

import groovy.transform.ToString

/**
 * @author Anatoliy Sokolenko
 */
@ToString
class Challenge {

    def UUID uuid

    def String text

    def UUID authorUuid

    def Set<UUID> inviteeUuids

}
