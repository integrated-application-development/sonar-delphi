unit ConstExpressionAmbiguity;

interface

// Previously there were grammar ambiguities between constExpression and expression.
// As a result, the following cases would match a record initialization instead of an expression
// This would cause errors once the "(record)" between the parentheses was parsed and the parser saw another operator.
const
  LooksLikeARecord = (SomeVariable * 100) + 0.5;
  LooksLikeARecordWithType: Integer = (SomeVariable - 60) * 42;
implementation

end.
