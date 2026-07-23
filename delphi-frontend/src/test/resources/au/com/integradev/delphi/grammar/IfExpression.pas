unit IfExpression;

interface

implementation

function Max(A, B: Integer): Integer;
begin
  Result := if A > B then A else B;
end;

procedure Test;
var
  X: Integer;
  S: string;
begin
  X := if X > 100 then 22 else 45;

  ShowMessage(if X < 100 then 'Small' else 'Big');

  // Nested if-expressions chain like Pascal's elseif.
  S := if X = 1 then 'One' else if X = 2 then 'Two' else 'Many';

  // Parentheses control how the low-priority `if` interacts with other operators.
  ShowMessage((if X < 100 then 'Small' else 'Large') + '!');

  // The condition and both branches may themselves be parenthesized or nested expressions.
  X := if (X > 0) then (X + 1) else (X - 1);
end;

end.
