package org.testcontainers.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import lombok.Getter;
import org.junit.After;

/**
 * @author ctayeb
 */
public abstract class AbstractCouchbaseTest {

    public static final String DEFAULT_BUCKET = "default";

    @Getter(lazy = true)
    private final static CouchbaseContainer couchbaseContainer = initCouchbaseContainer();

    @Getter(lazy = true)
    private final static Bucket bucket = openBucket(DEFAULT_BUCKET);

    @After
    public void clear() {
        if (getCouchbaseContainer().isIndex() && getCouchbaseContainer().isQuery() && getCouchbaseContainer().isPrimaryIndex()) {
            getBucket().query(
                    N1qlQuery.simple(String.format("DELETE FROM `%s`", getBucket().name()),
                            N1qlParams.build().consistency(ScanConsistency.STATEMENT_PLUS)));
        } else {
            getBucket().bucketManager().flush();
        }
    }

    private static CouchbaseContainer initCouchbaseContainer() {
        CouchbaseContainer couchbaseContainer = new CouchbaseContainer()
                .withNewBucket(DefaultBucketSettings.builder()
                        .enableFlush(true)
                        .name(DEFAULT_BUCKET)
                        .quota(100)
                        .replicas(0)
                        .type(BucketType.COUCHBASE)
                        .build());
        couchbaseContainer.start();
        return couchbaseContainer;
    }

    private static Bucket openBucket(String bucketName) {
        CouchbaseCluster cluster = getCouchbaseContainer().getCouchbaseCluster();
        Bucket bucket = cluster.openBucket(bucketName);
        Runtime.getRuntime().addShutdownHook(new Thread(bucket::close));
        return bucket;
    }
}
