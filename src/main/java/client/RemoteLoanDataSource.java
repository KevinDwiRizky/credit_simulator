package client;

import model.LoanRequest;

public class RemoteLoanDataSource implements LoanDataSource {

    private final HttpFetcher fetcher;
    private final String url;
    private final LoanRequestMapper mapper;

    public RemoteLoanDataSource(HttpFetcher fetcher, String url, LoanRequestMapper mapper) {
        this.fetcher = fetcher;
        this.url = url;
        this.mapper = mapper;
    }

    @Override
    public LoanRequest load() {
        return mapper.map(fetcher.get(url));
    }
}