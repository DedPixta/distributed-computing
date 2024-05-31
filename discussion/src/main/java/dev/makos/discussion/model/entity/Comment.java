package dev.makos.discussion.model.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table(value = "tbl_comment")
public class Comment {

    @PrimaryKey
    private CommentKey key;

    @Column
    private String content;

    public void setCountry(String country) {
        this.key.setCountry(country);
    }

    public void setTweetId(Long tweetId) {
        this.key.setTweetId(tweetId);
    }

    public void setId(Long id) {
        this.key.setId(id);
    }
}
