unit NotInOperator;

interface

implementation

type
  TFruit = (Apple, Orange, Banana);

function Simple(Fruit: TFruit): Boolean;
begin
  Result := Fruit not in [Apple, Orange];
end;

function CommentBetweenKeywords(Fruit: TFruit): Boolean;
begin
  Result := Fruit not {comment} in [Apple, Orange];
end;

function AdditiveOperandsBindTighter(B: Byte): Boolean;
begin
  Result := B + 1 not in [1] + [2];
end;

function UnaryNotBindsTighterThanNotIn(B: Byte): Boolean;
begin
  Result := not B not in [1, 254];
end;

end.
