package api;

import java.util.Set;

public interface UniterestService {
    Set<String> queryByOpenId(String openId);
}
