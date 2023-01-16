unit Unit2;

interface

uses
    Unit1;

type
  TFooHelper2 = class helper for TFoo
    function Bar: Boolean;
  end;

implementation

function TFooHelper2.Bar: Boolean;
begin
  // Do nothing
end;

end.