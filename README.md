# Distributed Ledger Project

This project is a distributed ledger application designed to handle transactions across multiple servers, leveraging gRPC for inter-server communication. It includes separate components for managing users, an admin interface, and a naming server to facilitate server discovery within the distributed network.

## Project Overview

The distributed ledger system consists of the following main servers:

- **DistLedgerServer**: The core server that processes and stores transactions in the ledger.
- **NamingServer**: Manages service discovery and helps clients locate the active DistLedgerServer instances.
- **AdminClient**: Provides administrative functionalities, allowing for configuration and monitoring of the distributed ledger system.
- **UserClient**: Allows end users to interact with the ledger, submitting and retrieving transactions.

Each server is configured to run independently and communicates with other servers through gRPC.

## Running the Servers

To start each server, use the following commands in the project root directory:

1. **DistLedgerServer**
   - This server handles the ledger's main operations and manages transaction storage and retrieval.
   - Run the following command:
     ```
     mvn exec:java -pl DistLedgerServer -Dexec.mainClass="pt.tecnico.distledger.server.DistLedgerServerMain"
     ```

2. **NamingServer**
   - Facilitates server discovery, helping clients locate active DistLedgerServer instances.
   - To start the NamingServer, use:
     ```
     mvn exec:java -pl NamingServer -Dexec.mainClass="pt.tecnico.distledger.namingserver.NamingServerMain"
     ```

3. **AdminClient**
   - The AdminClient allows administrators to configure and monitor the distributed ledger network.
   - To run the AdminClient, execute:
     ```
     mvn exec:java -pl Admin -Dexec.mainClass="pt.tecnico.distledger.adminclient.AdminClientMain"
     ```

4. **UserClient**
   - The UserClient provides an interface for end users to perform transactions and interact with the ledger.
   - Use this command to start the UserClient:
     ```
     mvn exec:java -pl User -Dexec.mainClass="pt.tecnico.distledger.userclient.UserClientMain"
     ```

## Project Structure

- **DistLedgerServer/**: Contains the server implementation for the distributed ledger.
- **NamingServer/**: Implements the naming server for service discovery.
- **Admin/**: Contains the code for the administrative client.
- **User/**: Holds the code for the user client.
- **Contract/**: Defines gRPC contracts for communication among servers and clients, generated from protocol buffer files.

### Additional Files

- **pom.xml**: Maven configuration file that manages dependencies and build configurations for all project modules.

---

This project can be extended to include additional functionalities like fault tolerance, replication, and enhanced transaction security. The modular structure allows for easy scaling and management of components within the distributed system.
