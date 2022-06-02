package coen317.project.documenteditor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NodeInfoResponse {
    int leader;
    Map<Integer, Integer> nodeMap;
}
