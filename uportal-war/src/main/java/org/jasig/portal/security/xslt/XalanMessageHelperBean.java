package org.jasig.portal.security.xslt;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Service;

@Service
public class XalanMessageHelperBean implements IXalanMessageHelper, MessageSourceAware {
    private MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(String code, String language) {
        final Locale locale = new Locale(language);
        final String message = messageSource.getMessage(code, null, locale);
        return message;
    }

}