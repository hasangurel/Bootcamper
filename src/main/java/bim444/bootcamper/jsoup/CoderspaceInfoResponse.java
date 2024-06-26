package bim444.bootcamper.jsoup;

import lombok.Builder;

@Builder
public record CoderspaceInfoResponse(
        String id,
        String name,
        String imgUrl,
        String link,
        String deadline,
        String description,
        String status,
        String eventType,
        Boolean isDead

) {
}
