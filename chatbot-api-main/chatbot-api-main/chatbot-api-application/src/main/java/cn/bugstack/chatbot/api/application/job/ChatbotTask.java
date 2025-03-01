package cn.bugstack.chatbot.api.application.job;

import cn.bugstack.chatbot.api.domain.ai.IOpenAI;
import cn.bugstack.chatbot.api.domain.zsxq.IZsxqApi;
import cn.bugstack.chatbot.api.domain.zsxq.model.aggregates.UnAnsweredQuestionsAggregates;
import cn.bugstack.chatbot.api.domain.zsxq.model.vo.Topics;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;


public class ChatbotTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ChatbotTask.class);

    private String groupName;
    private String groupId;
    private String cookie;
    private String openAiKey;
    private boolean silenced;

    private IZsxqApi zsxqApi;
    private IOpenAI openAI;

    public ChatbotTask(String groupName, String groupId, String cookie, String openAiKey, IZsxqApi zsxqApi, IOpenAI openAI, boolean silenced) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.cookie = cookie;
        this.openAiKey = openAiKey;
        this.zsxqApi = zsxqApi;
        this.openAI = openAI;
        this.silenced = silenced;
    }

    @Override
    public void run() {
        try {
            if (new Random().nextBoolean()) {
                logger.info("{} Closed randomly...", groupName);
                return;
            }

            GregorianCalendar calendar = new GregorianCalendar();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour > 22 || hour < 7) {
                logger.info("{} Closed！", groupName);
                return;
            }

            // 1. Inquiry question
            UnAnsweredQuestionsAggregates unAnsweredQuestionsAggregates = zsxqApi.queryUnAnsweredQuestionsTopicId(groupId, cookie);
            logger.info("{} inquiry result：{}", groupName, JSON.toJSONString(unAnsweredQuestionsAggregates));
            List<Topics> topics = unAnsweredQuestionsAggregates.getResp_data().getTopics();
            if (null == topics || topics.isEmpty()) {
                logger.info("{} There are no questions to be answered in this search", groupName);
                return;
            }

            // 2. AI answer
            Topics topic = topics.get(topics.size() - 1);
            String answer = openAI.doChatGPT(openAiKey, topic.getQuestion().getText().trim());
            // 3. Reply question
            boolean status = zsxqApi.answer(groupId, cookie, topic.getTopic_id(), answer, silenced);
            logger.info("{} ID：{} Q：{} A：{} S：{}", groupName, topic.getTopic_id(), topic.getQuestion().getText(), answer, status);
        } catch (Exception e) {
            logger.error("{} The automatic answer is abnormal", groupName, e);
        }
    }

}
