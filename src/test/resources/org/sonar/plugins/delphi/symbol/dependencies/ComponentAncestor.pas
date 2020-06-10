unit ComponentAncestor;

interface

uses
    Vcl.Controls
  , System.Classes
  ;
  
type
  TFooControl = class(TCustomControl)
  end;
  TFooComponent = class(TCustomControl)
    FControl: TFooControl;
  end;

implementation

end.