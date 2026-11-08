package co.appointment.service.grpc.client;

import co.appointment.grpc.BranchServiceGrpc;
import co.appointment.shared.service.grcp.AbstractGrpcBranchClient;
import org.springframework.stereotype.Service;

@Service
public class GrpcBranchService extends AbstractGrpcBranchClient {

    public GrpcBranchService(final BranchServiceGrpc.BranchServiceBlockingStub branchServiceBlockingStub) {
        super(branchServiceBlockingStub);
    }
}
