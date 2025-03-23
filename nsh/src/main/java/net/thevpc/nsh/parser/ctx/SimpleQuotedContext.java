package net.thevpc.nsh.parser.ctx;

import net.thevpc.nsh.parser.AbstractContext;
import net.thevpc.nsh.parser.NshParser;
import net.thevpc.nsh.parser.StrReader;
import net.thevpc.nsh.parser.Token;

public class SimpleQuotedContext extends AbstractContext {
    boolean processed=false;
    public SimpleQuotedContext(NshParser jshp) {
        super(jshp);
    }

    @Override
    public Token nextToken() {
        if(processed){
            return null;
        }
        StrReader reader = this.reader.strReader();
        StringBuilder sb=new StringBuilder();
        while(true){
            int r = reader.read();
            if (r < 0) {
                if(sb.length()==0){
                    return null;
                }
                break;
            }
            char rc=(char)r;
            if (rc == '\'') {
                break;
            }else{
                sb.append(rc);
            }
        }
        processed=true;
        return new Token("'",sb.toString(),"'"+sb.toString()+"'");
    }
}
