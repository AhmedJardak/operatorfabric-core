package org.lfenergy.operatorfabric.cards.publication.config.mongo;

import org.bson.Document;
import org.lfenergy.operatorfabric.cards.publication.model.I18n;
import org.lfenergy.operatorfabric.cards.publication.model.I18nPublicationData;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;

/**
 *
 * <p>Spring converter registered in mongo conversions</p>
 * <p>Converts {@link Document} to {@link I18n} using {@link I18nPublicationData} builder</p>
 *
 * @author David Binder
 */
public class I18nReadConverter implements Converter<Document, I18n> {
    @Override
    public I18n convert(Document source) {
        I18nPublicationData.I18nPublicationDataBuilder builder =
           I18nPublicationData.builder().key(source.getString("key"));
        Document parameters = (Document) source.get("parameters");
        if (parameters != null)
            for (Map.Entry<String, Object> e : parameters.entrySet()) {
                builder.parameter(e.getKey(), (String) e.getValue());
            }

        return builder.build();
    }
}
