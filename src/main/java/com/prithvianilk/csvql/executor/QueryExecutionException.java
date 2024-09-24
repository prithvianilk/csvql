package com.prithvianilk.csvql.executor;

public sealed class QueryExecutionException
        extends RuntimeException
        permits
        QueryExecutionException.FileDoesNotExist,
        QueryExecutionException.InvalidArgument,
        QueryExecutionException.UnhandledError {

    public static final class UnhandledError extends QueryExecutionException {
    }

    public static final class InvalidArgument extends QueryExecutionException {
    }

    public static final class FileDoesNotExist extends QueryExecutionException {
        private final String fileName;

        public FileDoesNotExist(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
