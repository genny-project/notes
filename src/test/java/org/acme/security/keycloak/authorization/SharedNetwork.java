package org.acme.security.keycloak.authorization;

import org.testcontainers.containers.Network;

public class SharedNetwork {

  static Network network = Network.newNetwork();
}
