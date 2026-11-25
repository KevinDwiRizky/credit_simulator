package client;

import model.LoanRequest;

public interface LoanDataSource {
    LoanRequest load();
}
