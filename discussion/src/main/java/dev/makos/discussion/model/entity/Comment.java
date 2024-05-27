package dev.makos.discussion.model.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table(value = "tbl_comment")
public class Comment {

    @PrimaryKeyColumn(name = "country", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String country;

    @PrimaryKeyColumn(name = "id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Long id;

    @PrimaryKeyColumn(name = "tweet_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private Long tweetId;

    @Column
    private String content;

}
